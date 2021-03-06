package com.x.wallet.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.btc.BtcAccountBalanceLoaderHelper;
import com.x.wallet.btc.BtcTransferHelper;
import com.x.wallet.btc.BtcUtils;
import com.x.wallet.btc.BuildBtcTxAsycTask;
import com.x.wallet.btc.PushBtcAsyncTask;
import com.x.wallet.lib.btc.TxBuildResult;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.EthTransactionFeeHelper;
import com.x.wallet.transaction.transfer.SendEthTransactionAsyncTask;
import com.x.wallet.transaction.transfer.ConfirmTransactionCallback;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.transaction.transfer.TransactionData;
import com.x.wallet.ui.data.RawAccountItem;
import com.x.wallet.ui.data.SerializableAccountItem;

import net.bither.bitherj.utils.Utils;

import java.math.BigDecimal;

/**
 * Created by Nick on 26/3/2018.
 */

public class TransferActivity extends WithBackAppCompatActivity {
    private EditText mToAddressEt;

    private EditText mTransferAmountEt;
    private TextView mUnitIndicatorTv;
    private TextView mAvailableBalanceTv;
    private ProgressDialog mProgressDialog;

    private TextView mPriceTv;
    private TextView mGasPriceUnitTv;

    private RawAccountItem mTokenItem;
    private SerializableAccountItem mAccountItem;
    private String mAddress;

    private EthTransactionFeeHelper mEthTransactionFeeHelper;
    private BtcTransferHelper mBtcTransferHelper;

    private static final int SCAN_ADDRESS_REQUEST_CODE = 1;
    private static final int CHOOSE_FAVORITE_ADDRESS_REQUEST_CODE = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_activity);

        if (getIntent().hasExtra(AppUtils.ACCOUNT_DATA)){
            mAccountItem = (SerializableAccountItem) getIntent().getSerializableExtra(AppUtils.ACCOUNT_DATA);
            mAddress = mAccountItem.getAddress();
        }

        if (getIntent().hasExtra(AppUtils.TOKEN_DATA)){
            mTokenItem = (RawAccountItem) getIntent().getSerializableExtra(AppUtils.TOKEN_DATA);
        }

        initView();
    }

    private void initView(){
        initToAddressView();
        initAmountView();
        initFeeView();
        initSendBtn();

        initData();
    }

    private void initToAddressView(){
        mProgressDialog = new ProgressDialog(this);
        mToAddressEt = findViewById(R.id.transfer_to_address);
        ImageButton chooseAddressBtn = findViewById(R.id.choose_address_bt);
        chooseAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action.CHOOSE_FAVORITE_ADDRESS_ACTION");
                intent.putExtra(AppUtils.COIN_TYPE, mTokenItem != null ? LibUtils.COINTYPE.COIN_ETH : mAccountItem.getCoinType());
                startActivityForResult(intent, CHOOSE_FAVORITE_ADDRESS_REQUEST_CODE);

            }
        });
    }

    private void initAmountView(){
        mTransferAmountEt = findViewById(R.id.transfer_to_amount);
        mUnitIndicatorTv = findViewById(R.id.unit_indicator);
        if (mTokenItem != null){
            mUnitIndicatorTv.setText(mTokenItem.getCoinName());
        } else {
            mUnitIndicatorTv.setText(mAccountItem.getCoinName());
        }
        mAvailableBalanceTv = findViewById(R.id.available_balance_tv);
    }

    private void initFeeView(){
        mPriceTv = findViewById(R.id.gas_price_tv);
        mGasPriceUnitTv = findViewById(R.id.gas_price_item);
        if(mTokenItem != null){
            mGasPriceUnitTv.setText(R.string.coin_unit_eth);
        } else {
            mGasPriceUnitTv.setText(mAccountItem.getCoinName());
        }
        final SeekBar gasPriceSeekBar = findViewById(R.id.gas_price_seekbar);
        gasPriceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i <= 33){
                    gasPriceSeekBar.setProgress(0);
                }
                if (i > 33 && i <= 67){
                    gasPriceSeekBar.setProgress(50);
                }
                if(i > 67 && i < 100){
                    gasPriceSeekBar.setProgress(100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateGasPrice(gasPriceSeekBar.getProgress());
            }
        });
    }

    private void initSendBtn(){
        Button sendBtn = findViewById(R.id.send_transfer);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_BTC){
                    handleBtcTransaction();
                } else {
                    handleTransaction();
                }
            }
        });
    }

    private void handleTransaction(){
        if (mToAddressEt.getText() == null || mToAddressEt.getText().length() < 40){
            responseForErrorResult(ERROR_TO_ADDRESS);
            return;
        }

        if (!checkSendOutAmount()){
            return;
        }

        BigDecimal amount;
        BigDecimal tokenFee = new BigDecimal(mPriceTv.getText().toString());
        if (mTokenItem == null){
            amount = new BigDecimal(TokenUtils.getBalanceText(mAccountItem.getBalance(), TokenUtils.ETH_DECIMALS));
            amount = amount.subtract(tokenFee);
        }else {
            amount = new BigDecimal(TokenUtils.getBalanceText(mTokenItem.getBalance(), mTokenItem.getDecimals()));
            //one condition: have enough Token, but insufficient ETH to pay for transfer fee...
            if (new BigDecimal(TokenUtils.getBalanceText(mAccountItem.getBalance(), TokenUtils.ETH_DECIMALS)).compareTo(tokenFee) < 0){
                responseForErrorResult(ERROR_INSUFFICIENT_ETH_FOR_FEE);
                return;
            }
        }

        if(!checkBalanceEnough(amount)){
            return;
        }

        //need to get gas price first
        if (new BigDecimal(mPriceTv.getText().toString()).equals(BigDecimal.ZERO)){
            Toast.makeText(TransferActivity.this,getResources().getString(R.string.wait_to_get_gas_price), Toast.LENGTH_SHORT).show();
            return;
        }

        final AlertDialog dialog = new AlertDialog.Builder(TransferActivity.this).create();
        final View dialogView = TransferActivity.this.getLayoutInflater().inflate(R.layout.password_confirm_dialog, null);
        dialog.setView(dialogView);
        dialog.show();

        Button cancelbtn = dialogView.findViewById(R.id.cancel_btn);
        final Button confirmBtn = dialogView.findViewById(R.id.confirm_btn);
        TextView dialogTitle = dialogView.findViewById(R.id.tv);
        dialogTitle.setText(getResources().getString(R.string.confirm_password));
        final EditText passwordEdit = dialogView.findViewById(R.id.password_et);

        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                confirmBtn.setEnabled(c != null && c.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = passwordEdit.getText().toString();
                TransactionData data = getTransactionData(mAddress, password);
                new SendEthTransactionAsyncTask(data, mTokenItem).execute(createConfirmTransactionCallback());
                dialog.dismiss();
                mProgressDialog.show();
                mProgressDialog.setCancelable(false);
            }
        });
    }

    private TransactionData getTransactionData(String address, String password){
        return new TransactionData(
                password, address,
                address, mToAddressEt.getText().toString(),
                mEthTransactionFeeHelper.getNowPrice(mPriceTv.getText().toString()).toBigInteger().toString(),
                mEthTransactionFeeHelper.getDefaultGasLimit(),
                mTransferAmountEt.getText().toString(), "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SCAN_ADDRESS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    String address = data.getStringExtra(AppUtils.EXTRA_ADDRESS);
                    mToAddressEt.setText(address);
                }
                break;
            case CHOOSE_FAVORITE_ADDRESS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    String address = data.getStringExtra(AppUtils.EXTRA_ADDRESS);
                    mToAddressEt.setText(address);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBtcTransferHelper != null){
            mBtcTransferHelper.destory();
        }
    }

    private ConfirmTransactionCallback<Boolean> createConfirmTransactionCallback(){
        return new ConfirmTransactionCallback<Boolean>() {
            @Override
            public void onTransactionConfirmed(Boolean result, final Throwable e) {
                mProgressDialog.dismiss();
                if (result){
                    TransferActivity.this.finish();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TransferActivity.this, "error" + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
    }

    private void initData(){
        if(mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_BTC){
            initBtc();
        } else {
            String indicator = "";
            if (mTokenItem != null){
                indicator = TokenUtils.getBalanceText(mTokenItem.getBalance(), mTokenItem.getDecimals()) + " " + mTokenItem.getCoinName();
            }else {
                indicator = TokenUtils.getBalanceText(mAccountItem.getBalance(), TokenUtils.ETH_DECIMALS) + " " + mAccountItem.getCoinName();
            }
            mAvailableBalanceTv.setText(indicator);

            mEthTransactionFeeHelper = new EthTransactionFeeHelper(new EthTransactionFeeHelper.OnPriceChangedListener() {
                @Override
                public void onPriceChanged(final String newPriceText) {
                    updatePriceText(newPriceText);
                }
            });
            mEthTransactionFeeHelper.getGasPrice();
        }
    }

    private void updateGasPrice(int progress) {
        if(mEthTransactionFeeHelper != null){
            mEthTransactionFeeHelper.updateGasPrice(progress);
        } else if(mBtcTransferHelper != null){
            mBtcTransferHelper.updateGasPrice(progress);
        }
    }

    private void updatePriceText(final String priceText){
        TransferActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPriceTv.setText(priceText);
            }
        });
    }

    private static final int ERROR_TO_ADDRESS = 0;
    private static final int ERROR_TO_AMOUNT = 1;
    private static final int ERROR_INSUFFICIENT_BALANCE = 2;
    private static final int ERROR_INSUFFICIENT_ETH_FOR_FEE = 3;
    private static final int ERROR_UNKNOWN = 4;

    private void responseForErrorResult(int errorCode){
        switch (errorCode){
            case ERROR_TO_ADDRESS:
                Toast.makeText(TransferActivity.this, getResources().getString(R.string.check_address_error), Toast.LENGTH_SHORT).show();
                break;
            case ERROR_TO_AMOUNT:
                Toast.makeText(TransferActivity.this, getResources().getString(R.string.invalidate_balance), Toast.LENGTH_SHORT).show();
                break;
            case ERROR_INSUFFICIENT_BALANCE:
                Toast.makeText(TransferActivity.this, getResources().getString(R.string.insufficient_balance), Toast.LENGTH_SHORT).show();
                break;
            case ERROR_INSUFFICIENT_ETH_FOR_FEE:
                Toast.makeText(TransferActivity.this, getResources().getString(R.string.insufficient_token_fee), Toast.LENGTH_SHORT).show();
                break;
            case ERROR_UNKNOWN:
                Toast.makeText(TransferActivity.this, getResources().getString(R.string.insufficient_token_fee), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean checkSendOutAmount(){
        if (TextUtils.isEmpty(mTransferAmountEt.getText())
                || !AppUtils.isValideNumber(mTransferAmountEt.getText().toString())
                || new BigDecimal(mTransferAmountEt.getText().toString()).equals(BigDecimal.ZERO)){
            Log.i(AppUtils.APP_TAG,"TransferActivity checkAmount invalid amount = " + mTransferAmountEt.getText().toString());
            responseForErrorResult(ERROR_TO_AMOUNT);
            return false;
        }

        return true;
    }

    private boolean checkBalanceEnough(BigDecimal amount){
        if (new BigDecimal(mTransferAmountEt.getText().toString()).compareTo(amount) > 0){
            responseForErrorResult(ERROR_INSUFFICIENT_BALANCE);
            return false;
        }
        return true;
    }

    private void initBtc(){
        mBtcTransferHelper = new BtcTransferHelper(new BtcTransferHelper.OnTransactionFeeRequestFinishedListener() {
            @Override
            public void onFeeRequestFinished(final String priceText) {
                updatePriceText(priceText);
            }
        });
        mBtcTransferHelper.loadBalance(this, getLoaderManager(), mAccountItem.getAddress(), new BtcAccountBalanceLoaderHelper.OnDataLoadFinishedListener() {
            @Override
            public void onBalanceLoadFinished(String balance) {
                mAvailableBalanceTv.setText(balance + " " + mAccountItem.getCoinName());
            }
        });
        mBtcTransferHelper.getTransactionFee();
    }

    private void handleBtcTransaction(){
        if(!Utils.validBicoinAddress(mToAddressEt.getText().toString())){
            responseForErrorResult(ERROR_TO_ADDRESS);
            return;
        }

        if (!checkSendOutAmount()){
            return;
        }
        //Log.i("testBtcTx", "TransferActivity handleBtcTransaction out = " + transferAmount.getText().toString());
        //Log.i("testBtcTx", "TransferActivity handleBtcTransaction balance = " + mBtcTransferHelper.getBalance());
        //Log.i("testBtcTx", "TransferActivity handleBtcTransaction getCurrentFeeBase = " + mBtcTransferHelper.getCurrentFeeBase());
        if(!checkBalanceEnough(mBtcTransferHelper.getBalance())){
            return;
        }
        long sendOutAmount = TokenUtils.translateToRaw(mTransferAmountEt.getText().toString(), BtcUtils.BTC_DECIMALS_COUNT).longValue();
        new BuildBtcTxAsycTask(this, sendOutAmount, mAddress,
                mToAddressEt.getText().toString(), mAddress, mBtcTransferHelper.getCurrentFeeBase(),
                new BuildBtcTxAsycTask.OnTxBuildFinishedListener() {
                    @Override
                    public void onTxBuildFinished(TxBuildResult txBuildResult) {
                        responseForErrorResult(ERROR_UNKNOWN);
                    }

                    @Override
                    public void onTxPushFinished(int resultCode) {
                        if(resultCode == PushBtcAsyncTask.PushOutTxResult.RESULT_OK){
                            TransferActivity.this.finish();
                        }
                    }
                }).execute();

    }

    private void scanAddress(){
        Intent intent = new Intent(TransferActivity.this, ScanAddressQRActivity.class);
        intent.putExtra(AppUtils.COIN_TYPE, mTokenItem != null ? LibUtils.COINTYPE.COIN_ETH : mAccountItem.getCoinType());
        startActivityForResult(intent, SCAN_ADDRESS_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.scan_address_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.scan_address_to_action:
                scanAddress();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
