package com.x.wallet.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.util.ExchangeCalUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
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
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by zhangxing on 18-3-21.
 */

public class SendTransactionService extends IntentService {

    public final static String FROM_ADDRESS_TAG     = "from_address_tag";
    public final static String TO_ADDRESS_TAG       = "to_address_tag";
    public final static String GAS_PRICE_TAG        = "gas_price_tag";
    public final static String GAS_LIMIT_TAG        = "gas_limit_tag";
    public final static String AMOUNT_TAG           = "amount_tag";
    public final static String EXTRA_DATA_TAG       = "extra_data_tag";
    public final static String PASSWORD_TAG         = "password_tag";
    public final static String TOKEN20_TYPE_NAME    = "token20_name";
    public final static String TOKEN20_ADDRESS_TAG  = "token20_address";
    public final static String TOKEN20_DECIMALS_TAG = "token20_decimals";

    private final BigInteger defaultGasLimit = new BigInteger("21000");
    private final BigInteger defaultTokenGasLimit = new BigInteger("91000");

    public SendTransactionService(){
        super("SendTransactionService");
    }

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


        if (intent.hasExtra(TOKEN20_TYPE_NAME) && intent.hasExtra(TOKEN20_ADDRESS_TAG) && intent.hasExtra(TOKEN20_DECIMALS_TAG)){  //token transfer
            String token20Name = intent.getStringExtra(TOKEN20_TYPE_NAME);
            String token20Address  = intent.getStringExtra(TOKEN20_ADDRESS_TAG);
            int token20Decimals = intent.getIntExtra(TOKEN20_DECIMALS_TAG, 1);
            try {
                sendTokenTransaction(fromAddress, toAddress, token20Address, credentials, gasPrice, defaultTokenGasLimit, amount, token20Decimals);
            } catch (CipherException e){

            }
        }else { // normal transfer
            try {
                sendTransaction(fromAddress, toAddress, credentials, gasPrice, gasLimit, amount, extraData);
            } catch (CipherException e) {

            }
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
        Log.i(AppUtils.APP_TAG, "transaction service get credential ok");

        return credentials;
    }

    private void sendTransaction(final String address, final String toAddress, final Credentials credentials,
            final String gasPrice, final String gasLimit, final String amount, final String extraData) throws CipherException{

        try {
            EtherscanAPI.getInstance().getNonceForAddress(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    toastNetworkError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String result = jsonObject.getString("result").substring(2);
                        BigInteger nonce = new BigInteger(result, 16);
                        BigInteger gasP = new BigInteger(gasPrice);

                        RawTransaction tx = getRawTransaction(
                                nonce,
                                gasP,
                                defaultGasLimit,
                                toAddress,
                                new BigDecimal(amount).multiply(ExchangeCalUtil.ONE_ETHER).toBigInteger(),
                                extraData);

                        byte[] signed = TransactionEncoder.signMessage(tx ,(byte) 1 ,credentials);

                    pushTransaction(signed);
                    }catch (JSONException e){
                        Toast.makeText(SendTransactionService.this,"get nonce for transaction error", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        } catch (IOException e){

        }

    }

    private void sendTokenTransaction(final String address, final String toAddress, final String contractAddress,
                                      final Credentials credentials, final String gasPrice, final BigInteger gasLimit, final String amount, final int decimals) throws CipherException{
        try {
            EtherscanAPI.getInstance().getNonceForAddress(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    toastNetworkError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        String result = object.getString("result").substring(2);
                        BigInteger nonce = new BigInteger(result, 16);
                        String extraData = packageTokenData(toAddress, amount, decimals);
                        //transfer amount is in the extra data, not in raw transaction.
                        RawTransaction transaction = getRawTransaction(nonce, new BigInteger(gasPrice), gasLimit, contractAddress, BigInteger.ZERO, extraData);

                        byte[] singedMessage = TransactionEncoder.signMessage(transaction, (byte) 1, credentials);

                        pushTransaction(singedMessage);
                    }catch (JSONException e){
                        Toast.makeText(SendTransactionService.this,"get nonce for transaction error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (IOException e){

        }
    }

    private void pushTransaction(byte[] singedMessage){
        try {
            EtherscanAPI.getInstance().forwardTransaction("0x" + Hex.toHexString(singedMessage), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    toastNetworkError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String receive = response.body().string();
                    try {
                        JSONObject object = new JSONObject(receive);
                        String result = object.getString("result");
                        if (result != null){
                            toastSuccess(result);
                        }else {
                            String errorMsg = object.getJSONObject("error").getString("message");
                            toastFailError(errorMsg);
                        }
                    }catch (JSONException e){
                        toastFailError("unknown error! Please check transfer details later!");
                    }
                }
            });
        } catch (IOException e){

        }
    }

    private RawTransaction getRawTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String toAddress, BigInteger amount, String extraData){
        return RawTransaction.createTransaction(nonce, gasPrice, gasLimit, toAddress, amount, extraData);
    }

    //package data for send token transaction
    private String packageTokenData(final String toAddress, final String amount, final int decimals){

        String methodName = "transfer";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        Address sendToAddress = new Address(toAddress);

        BigInteger tokenBaseUnit = BigInteger.TEN.pow(decimals);
        BigInteger out = new BigDecimal(amount).multiply(new BigDecimal(tokenBaseUnit)).toBigInteger();
        Uint256 value = new Uint256(out);
        inputParameters.add(sendToAddress);
        inputParameters.add(value);

        TypeReference<Bool> typeReference = new TypeReference<Bool>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        return FunctionEncoder.encode(function);
    }

    private void toastSuccess(String msg){
        Toast.makeText(this, "send transfer success! receipt : " + msg, Toast.LENGTH_LONG).show();
    }

    private void toastNetworkError(){
        Toast.makeText(this, "connect to network error", Toast.LENGTH_LONG).show();
    }

    private void toastFailError(String msg){
        Toast.makeText(this, "transfer fail for " + msg, Toast.LENGTH_LONG).show();
    }
}
