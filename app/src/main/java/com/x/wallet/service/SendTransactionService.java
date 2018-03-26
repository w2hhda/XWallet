package com.x.wallet.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.util.ExchangeCalUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.request.RawTransaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by zhangxing on 18-3-21.
 */

public class SendTransactionService extends IntentService {

    public final static String FROM_ADDRESS_TAG = "from_address_tag";
    public final static String TO_ADDRESS_TAG = "to_address_tag";
    public final static String GAS_PRICE_TAG  = "gas_price_tag";
    public final static String GAS_LIMIT_TAG  = "gas_limit_tag";
    public final static String AMOUNT_TAG = "amount_tag";
    public final static String EXTRA_DATA_TAG = "extra_data_tag";
    public final static String PASSWORD_TAG = "password_tag";


    public SendTransactionService(){
        super("SendTransactionService");
    }

    private final String defaultGasPrice = "0x4a817c800";
    private final String defaultGasLimit = "47e7c4";

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String fromAddress = intent.getStringExtra(FROM_ADDRESS_TAG);
        String toAddress   = intent.getStringExtra(TO_ADDRESS_TAG);
        String gasPrice    = intent.getStringExtra(GAS_PRICE_TAG);
        String gasLimit    = intent.getStringExtra(GAS_LIMIT_TAG);
        String amount      = intent.getStringExtra(AMOUNT_TAG);
        String extraData   = intent.getStringExtra(EXTRA_DATA_TAG);
        String password    = intent.getStringExtra(PASSWORD_TAG);

        Credentials credentials = getCredential(fromAddress, password);

        try{
            sendTransaction(fromAddress, toAddress,credentials, gasPrice, gasLimit, amount, extraData);
        }catch (CipherException e){

        }

    }

    private Credentials getCredential(String address, String password){
        String keyStore;
        Credentials credentials = null;
        Cursor cursor = XWalletApplication.getApplication().getContentResolver().query(XWalletProvider.CONTENT_URI,
                new String[]{DbUtils.DbColumns.ADDRESS,DbUtils.DbColumns.KEYSTORE},null, null, null);
        while (cursor.moveToNext()){

            if (cursor.getString(0).equalsIgnoreCase(address)){
                keyStore = cursor.getString(1);
                if (keyStore == null){
                    return  null;
                }

                ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

                try {
                    WalletFile walletFile = mapper.readValue(keyStore, WalletFile.class);
                    credentials = Credentials.create(Wallet.decrypt(password, walletFile));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CipherException e){

                }
            }

        }
        Log.i("@@@@","privateKey = " + credentials.getEcKeyPair().getPrivateKey().toString(16));

        return credentials;
    }

    private void sendTransaction(final String address, final String toAddress, final Credentials credentials,
            final String gasPrice, final String gas_limit, final String amount, final String extraData) throws CipherException{

        try {
            EtherscanAPI.getInstance().getNonceForAddress(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("@@@@","error: " + call.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String result = jsonObject.getString("result").substring(2);
                        BigInteger nonce = new BigInteger(result, 16);

                        BigInteger gasP = new BigInteger(gasPrice, 16);

                        RawTransaction tx = RawTransaction.createTransaction(
                                nonce,
                                gasP,
                                new BigInteger(gas_limit, 16),
                                toAddress,
                                new BigDecimal(amount).multiply(ExchangeCalUtil.ONE_ETHER).toBigInteger(),
                                extraData);

                        Log.i("@@@@",
                                "Nonce: " + tx.getNonce() + "\n" +
                                        "gasPrice: " + tx.getGasPrice() + "\n" +
                                        "gasLimit: " + tx.getGasLimit() + "\n" +
                                        "To: " + tx.getTo() + "\n" +
                                        "Amount: " + tx.getValue() + "\n" +
                                        "Data: " + tx.getData()
                        );

                        Log.i("@@@@","tx = " + tx.toString());
                        byte[] signed = TransactionEncoder.signMessage(tx  ,credentials);

                    pushTransaction(signed);
                    }catch (JSONException e){

                    }

                }
            });
        } catch (IOException e){

        }

    }

    private void pushTransaction(byte[] singedMessage){
        try {
            EtherscanAPI.getInstance().forwardTransaction("0x" + Hex.toHexString(singedMessage), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String receive = response.body().string();
                    try {
                        JSONObject object = new JSONObject(receive);
                        Log.i("@@@@", "result = " + object.getString("result"));
                    }catch (JSONException e){
                        Log.i("@@@@", "result = " + receive);
                    }
                }
            });
        } catch (IOException e){

        }
    }
}
