package com.x.wallet.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.R;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.util.ExchangeCalUtil;
import com.x.wallet.service.SendTransactionService;
import com.x.wallet.transaction.address.ConfirmPasswordAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Nick on 26/3/2018.
 */

public class TransferActivity extends WithBackAppCompatActivity {
    private final BigInteger defaultGasLimit = new BigInteger("21000");
    private EditText toAddress;
    private TextView priceTv;
    private EditText transferAmount;
    private BigDecimal defaultPrice = new BigDecimal(0);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_activity);
        String address = "";
        Intent intent = getIntent();
        if (intent != null){
            address = intent.getStringExtra(AccountDetailActivity.SHARE_ADDRESS_EXTRA);
        }
        if (address != "") {
            initView(address);
        }else {
            Toast.makeText(this, "error address!", Toast.LENGTH_SHORT);
        }
    }

    private void initView(final String address){
        this.setTitle(getResources().getString(R.string.send_out_transaction));

        toAddress = findViewById(R.id.transfer_to_address);
        priceTv = findViewById(R.id.gas_price_tv);
        transferAmount = findViewById(R.id.transfer_to_amount);
        ImageButton scanBtn = findViewById(R.id.wallet_scan);
        Button sendBtn = findViewById(R.id.send_transfer);
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
                final AlertDialog dialog = new AlertDialog.Builder(TransferActivity.this).create();
                final View dialogView = TransferActivity.this.getLayoutInflater().inflate(R.layout.confirm_password_dialog, null);
                dialog.setView(dialogView);
                dialog.show();

                Button cancelbtn = dialogView.findViewById(R.id.DialogCancel);
                Button confirmBtn = dialogView.findViewById(R.id.DialogConfirm);
                final EditText passwordEdit = dialogView.findViewById(R.id.dialogPasswordEdit);

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
        Log.i("@@@@","price = " + getNowPrice());

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

                        Log.i("@@@@","result gas resukt = " + result + "price = " + price);
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
}
