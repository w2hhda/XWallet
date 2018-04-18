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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.XWalletApplication;
import com.x.wallet.btc.BtcAccountBalanceLoaderHelper;
import com.x.wallet.btc.BtcTransferHelper;
import com.x.wallet.btc.BtcUtils;
import com.x.wallet.btc.BuildBtcTxAsycTask;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.EthTransactionFeeHelper;
import com.x.wallet.transaction.address.ConfirmPasswordAsyncTask;
import com.x.wallet.transaction.address.ConfirmTransactionCallback;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.ui.data.RawAccountItem;
import com.x.wallet.ui.data.SerializableAccountItem;

import net.bither.bitherj.core.Tx;
import net.bither.bitherj.utils.Utils;

import java.math.BigDecimal;

/**
 * Created by Nick on 26/3/2018.
 */

public class TransferActivity extends WithBackAppCompatActivity {
    private EditText toAddress;

    private EditText transferAmount;
    private TextView unitIndicator;
    private TextView availableBalance;
    private ProgressDialog mProgressDialog;

    private TextView priceTv;
    private TextView mGasPriceUnitTv;

    private RawAccountItem mTokenItem;
    private SerializableAccountItem mAccountItem;
    private String mAddress;

    private EthTransactionFeeHelper mEthTransactionFeeHelper;
    private BtcTransferHelper mBtcTransferHelper;

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
        toAddress = findViewById(R.id.transfer_to_address);
        ImageButton scanBtn = findViewById(R.id.wallet_scan);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TransferActivity.this, ScanAddressQRActivity.class);
                startActivityForResult(intent, ScanAddressQRActivity.REQUEST_CODE);
            }
        });
    }

    private void initAmountView(){
        transferAmount = findViewById(R.id.transfer_to_amount);
        unitIndicator = findViewById(R.id.unit_indicator);
        if (mTokenItem != null){
            unitIndicator.setText(mTokenItem.getCoinName());
        } else {
            unitIndicator.setText(mAccountItem.getCoinName());
        }
        availableBalance = findViewById(R.id.available_balance);
    }

    private void initFeeView(){
        priceTv = findViewById(R.id.gas_price_tv);
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
        if (toAddress.getText() == null || toAddress.getText().length() < 40){
            responseForErrorResult(ERROR_TO_ADDRESS);
            return;
        }

        if (!checkSendOutAmount()){
            return;
        }

        BigDecimal amount;
        BigDecimal tokenFee = new BigDecimal(priceTv.getText().toString());
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
        if (new BigDecimal(priceTv.getText().toString()).equals(BigDecimal.ZERO)){
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
                Intent intent = getIntentForSend(mAddress, password);
                new ConfirmPasswordAsyncTask(intent, password, mAddress).execute(createConfirmTransactionCallback());
                dialog.dismiss();
                mProgressDialog.show();
            }
        });
    }

    private Intent getIntentForSend(String address, String password){
        Intent intent = new Intent();
        intent.putExtra(ConfirmPasswordAsyncTask.FROM_ADDRESS_TAG, address);
        intent.putExtra(ConfirmPasswordAsyncTask.TO_ADDRESS_TAG, toAddress.getText().toString());
        intent.putExtra(ConfirmPasswordAsyncTask.PASSWORD_TAG, password);
        intent.putExtra(ConfirmPasswordAsyncTask.GAS_PRICE_TAG, mEthTransactionFeeHelper.getNowPrice(priceTv.getText().toString()).toBigInteger().toString());
        intent.putExtra(ConfirmPasswordAsyncTask.GAS_LIMIT_TAG, mEthTransactionFeeHelper.getDefaultGasLimit());
        intent.putExtra(ConfirmPasswordAsyncTask.AMOUNT_TAG, transferAmount.getText().toString());
        intent.putExtra(ConfirmPasswordAsyncTask.EXTRA_DATA_TAG, "");
        if (mAccountItem != null){
            intent.putExtra(AppUtils.ACCOUNT_DATA, mAccountItem);
        }

        if (mTokenItem != null){
            intent.putExtra(AppUtils.TOKEN_DATA, mTokenItem);
        }

        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanAddressQRActivity.REQUEST_CODE){
            if (resultCode == RESULT_OK){
                String address = data.getStringExtra(ScanAddressQRActivity.EXTRA_ADDRESS);
                toAddress.setText(address);
            }
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
                    XWalletApplication.getApplication().getBalanceLoaderManager().getAllBalance(null, false);
                    Intent newIntent = new Intent("com.x.wallet.action.SEE_ACCOUNT_DETAIL_ACTION");
                    newIntent.putExtra(AppUtils.ACCOUNT_DATA, mAccountItem);
                    if (mTokenItem != null){
                        //RawAccountItem mTokenItem = (RawAccountItem)intent.getSerializableExtra(AppUtils.TOKEN_DATA);
                        newIntent.putExtra(AppUtils.TOKEN_DATA, mTokenItem);
                    }
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    TransferActivity.this.startActivity(newIntent);
                }else {
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
            availableBalance.setText(indicator);

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
                priceTv.setText(priceText);
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
        if (TextUtils.isEmpty(transferAmount.getText())
                || !AppUtils.isValideNumber(transferAmount.getText().toString())
                || new BigDecimal(transferAmount.getText().toString()).equals(BigDecimal.ZERO)){
            Log.i(AppUtils.APP_TAG,"TransferActivity checkAmount invalid amount = " + transferAmount.getText().toString());
            responseForErrorResult(ERROR_TO_AMOUNT);
            return false;
        }

        return true;
    }

    private boolean checkBalanceEnough(BigDecimal amount){
        if (new BigDecimal(transferAmount.getText().toString()).compareTo(amount) > 0){
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
                availableBalance.setText(balance + " " + mAccountItem.getCoinName());
            }
        });
        mBtcTransferHelper.getTransactionFee();
    }

    private void handleBtcTransaction(){
        if(!Utils.validBicoinAddress(toAddress.getText().toString())){
            responseForErrorResult(ERROR_TO_ADDRESS);
            return;
        }

        if (!checkSendOutAmount()){
            return;
        }
        /*Log.i("test", "TransferActivity handleBtcTransaction out = " + transferAmount.getText().toString());
        Log.i("test", "TransferActivity handleBtcTransaction balance = " + mBtcTransferHelper.getBalance());*/
        if(!checkBalanceEnough(mBtcTransferHelper.getBalance())){
            return;
        }
        BigDecimal amount = new BigDecimal(transferAmount.getText().toString());
        long sendOutAmount = TokenUtils.translateToRaw(transferAmount.getText().toString(), BtcUtils.BTC_DECIMALS_COUNT).longValue();
        new BuildBtcTxAsycTask(this, sendOutAmount, mAddress,
                toAddress.getText().toString(), mAddress, mBtcTransferHelper.getCurrentFeeBase(),
                new BuildBtcTxAsycTask.OnTxBuildFinishedListener() {
                    @Override
                    public void onTxBuildFinished(Tx tx) {
                        responseForErrorResult(ERROR_UNKNOWN);
                    }
                }).execute();

    }
}
