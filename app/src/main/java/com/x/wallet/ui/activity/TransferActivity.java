package com.x.wallet.ui.activity;

import android.app.AlertDialog;
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
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.util.ExchangeCalUtil;
import com.x.wallet.service.SendTransactionService;
import com.x.wallet.transaction.address.ConfirmPasswordAsyncTask;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.ui.data.RawAccountItem;
import com.x.wallet.ui.data.SerializableAccountItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Nick on 26/3/2018.
 */

public class TransferActivity extends WithBackAppCompatActivity {
    private BigInteger defaultGasLimit = new BigInteger("91000");
    private EditText toAddress;
    private TextView priceTv;
    private EditText transferAmount;
    private BigDecimal defaultPrice = new BigDecimal(0);
    private RawAccountItem mTokenItem;
    private TextView unitIndicator;
    private SerializableAccountItem mAccountItem;
    private TextView availableBalance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_activity);
        String address = "";

        if (getIntent().hasExtra(AppUtils.ACCOUNT_DATA)){
            mAccountItem = (SerializableAccountItem) getIntent().getSerializableExtra(AppUtils.ACCOUNT_DATA);
            address = mAccountItem.getAddress();
        }

        if (getIntent().hasExtra(AppUtils.TOKEN_DATA)){
            mTokenItem = (RawAccountItem) getIntent().getSerializableExtra(AppUtils.TOKEN_DATA);
        }

        initView(address);
    }

    private void initView(final String address){
        this.setTitle(getResources().getString(R.string.send_out_transaction));

        toAddress = findViewById(R.id.transfer_to_address);
        priceTv = findViewById(R.id.gas_price_tv);
        transferAmount = findViewById(R.id.transfer_to_amount);
        unitIndicator = findViewById(R.id.unit_indicator);
        ImageButton scanBtn = findViewById(R.id.wallet_scan);
        Button sendBtn = findViewById(R.id.send_transfer);
        availableBalance = findViewById(R.id.available_balance);
        if (mTokenItem != null){
            unitIndicator.setText(mTokenItem.getCoinName());
        }
        
        StringBuilder indicator= new StringBuilder("Max: ");
        if (mTokenItem != null){
            indicator.append(TokenUtils.getBalanceText(mTokenItem.getBalance(), mTokenItem.getDecimals()) );
            indicator.append(" ");
            indicator.append(mTokenItem.getCoinName());
        }else {
            indicator.append(TokenUtils.getBalanceText(mAccountItem.getBalance(), TokenUtils.ETH_DECIMALS));
            indicator.append(" ");
            indicator.append(mAccountItem.getCoinName());
        }
        availableBalance.setText(indicator);

        getGasPrice();

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TransferActivity.this, ScanAddressQRActivity.class);
                startActivityForResult(intent, ScanAddressQRActivity.REQUEST_CODE);
            }
        });

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
                updateGasPrice(TransferActivity.this, gasPriceSeekBar.getProgress());
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toAddress.getText() == null || toAddress.getText().length() < 40){
                    Toast.makeText(TransferActivity.this, getResources().getString(R.string.check_address_error), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (transferAmount.getText() == null || transferAmount.getText().length() == 0
                        || !isValideNumber(transferAmount.getText().toString())
                        || new BigDecimal(transferAmount.getText().toString()).equals(BigDecimal.ZERO)){
                    Toast.makeText(TransferActivity.this, getResources().getString(R.string.invalidate_balance), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(TransferActivity.this, getResources().getString(R.string.insufficient_token_fee), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (new BigDecimal(transferAmount.getText().toString()).compareTo(amount) > 0){
                    Toast.makeText(TransferActivity.this, getResources().getString(R.string.insufficient_balance), Toast.LENGTH_SHORT).show();
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
                        Intent intent = getIntentForSend(address, password);
                        new ConfirmPasswordAsyncTask(TransferActivity.this, intent, password, address).execute();
                        dialog.dismiss();
                    }
                });
            }
        });

    }

    private Intent getIntentForSend(String address, String password){
        Intent intent = new Intent(TransferActivity.this, SendTransactionService.class);
        intent.putExtra(SendTransactionService.FROM_ADDRESS_TAG, address);
        intent.putExtra(SendTransactionService.TO_ADDRESS_TAG, toAddress.getText().toString());
        intent.putExtra(SendTransactionService.PASSWORD_TAG, password);
        intent.putExtra(SendTransactionService.GAS_PRICE_TAG, getNowPrice().toBigInteger().toString());
        intent.putExtra(SendTransactionService.GAS_LIMIT_TAG, defaultGasLimit);
        intent.putExtra(SendTransactionService.AMOUNT_TAG, transferAmount.getText().toString());
        intent.putExtra(SendTransactionService.EXTRA_DATA_TAG, "");
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

    private void getGasPrice(){
        try {
            EtherscanAPI.getInstance().getGasPrice(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        final String result = object.getString("result").substring(2);
                        final BigInteger price = new BigInteger(result, 16);

                        Log.i(AppUtils.APP_TAG,"gas price = " + price);
                        TransferActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BigDecimal nowPrice = ExchangeCalUtil.getInstance().weiToEther(price.multiply(defaultGasLimit));
                                setDefaultPrice(nowPrice);
                                priceTv.setText(nowPrice + "");
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (IOException e){

        }
    }

    private void setDefaultPrice(BigDecimal number){
        this.defaultPrice = number;
    }

    private BigDecimal getDefaultPrice(){
        return defaultPrice;
    }

    private BigDecimal getNowPrice(){
        BigDecimal ethTowei = new BigDecimal("1000000000000000000");
        return new BigDecimal(priceTv.getText().toString()).multiply(ethTowei).divide(new BigDecimal(defaultGasLimit));
    }

    private void updateGasPrice(TransferActivity activity, final int progress){
        final BigDecimal half = new BigDecimal(0.5);
        final BigDecimal dec  = new BigDecimal(10);
        final BigDecimal nowPrice = getDefaultPrice();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progress == 0){
                    BigDecimal now = nowPrice.multiply(half);
                    priceTv.setText(now.toString());
                }else if (progress == 100){
                    BigDecimal now = nowPrice.multiply(dec);

                    priceTv.setText(now.toString());
                }else {
                    BigDecimal now = nowPrice;
                    priceTv.setText(now.toString());
                }


            }
        });
    }

    private Boolean isValideNumber(String str){
        Pattern pattern = Pattern.compile("[0-9]+.?[0-9]+");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
}
