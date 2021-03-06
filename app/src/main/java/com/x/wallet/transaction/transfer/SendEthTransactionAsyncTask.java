package com.x.wallet.transaction.transfer;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.util.ExchangeCalUtil;
import com.x.wallet.ui.data.RawAccountItem;

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
import org.web3j.crypto.ECKeyPair;
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
 * Created by Nick on 26/3/2018.
 */

public class SendEthTransactionAsyncTask extends AsyncTask<ConfirmTransactionCallback, Void, Void> {
    private String password;
    private String address;

    private String fromAddress;
    private String toAddress;
    private String gasPrice;
    private String gasLimit;
    private String amount;
    private String extraData;
    private String token20Address;

    private ContentValues values;

    RawAccountItem mTokenItem = null;

    private final BigInteger defaultGasLimit = new BigInteger("91000");

    public SendEthTransactionAsyncTask(TransactionData data, RawAccountItem tokenItem){
        this.password = data.getPassword();
        this.address = data.getAddress();
        fromAddress =  data.getFromAddress();
        toAddress   = data.getToAddress();
        gasPrice    = data.getGasPrice();
        gasLimit    = data.getGasLimit().toString();
        amount      = data.getAmount();
        extraData   = data.getExtraData();
        mTokenItem = tokenItem;
    }

    @Override
    protected Void doInBackground(ConfirmTransactionCallback... params ) {
        ConfirmTransactionCallback callback = params[0];
        String keyStore = queryKeyStore();
        if (TextUtils.isEmpty(keyStore)){
            handleErrorCallback(callback, null);
            Log.w(AppUtils.APP_TAG, "SendEthTransactionAsyncTask doInBackground keyStore is empty");
            return null;
        }

        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

        try {
            WalletFile walletFile = mapper.readValue(keyStore, WalletFile.class);
            ECKeyPair keyPair = Wallet.decrypt(password, walletFile);
            if (keyPair == null){
                handleErrorCallback(callback, null);
                Log.w(AppUtils.APP_TAG, "SendEthTransactionAsyncTask doInBackground keyPair is null");
                return null;
            }

            WalletFile file = Wallet.createStandard(password, keyPair);
            String newAddress = "0x" +file.getAddress();
            if (!newAddress.equalsIgnoreCase(address)){
                handleErrorCallback(callback, null);
                Log.w(AppUtils.APP_TAG, "SendEthTransactionAsyncTask doInBackground address not the same!");
                return null;
            }

            Credentials credentials = Credentials.create(Wallet.decrypt(password, walletFile));
            prepareToSend(credentials, callback);
        } catch (IOException e) {
            handleErrorCallback(callback, e);
            Log.e(AppUtils.APP_TAG, "SendEthTransactionAsyncTask doInBackground IOException", e);
        }catch (CipherException e){
            handleErrorCallback(callback, e);
            Log.e(AppUtils.APP_TAG, "SendEthTransactionAsyncTask doInBackground CipherException", e);
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //mProgressDialog.show();

    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        //mProgressDialog.dismiss();
//        if (s){
//            //mContext.startService(intent);
//            SerializableAccountItem accountItem = (SerializableAccountItem) intent.getSerializableExtra(AppUtils.ACCOUNT_DATA);
//            Intent newIntent = new Intent("com.x.wallet.action.SEE_ACCOUNT_DETAIL_ACTION");
//            newIntent.putExtra(AppUtils.ACCOUNT_DATA, accountItem);
//            if (intent.hasExtra(AppUtils.TOKEN_DATA)){
//                RawAccountItem mTokenItem = (RawAccountItem)intent.getSerializableExtra(AppUtils.TOKEN_DATA);
//                newIntent.putExtra(AppUtils.TOKEN_DATA, mTokenItem);
//            }
//            newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            mContext.startActivity(newIntent);
//
//        }else {
//            Toast.makeText(mContext, "error password", Toast.LENGTH_SHORT).show();
//        }

    }

    private void prepareToSend(Credentials credentials, ConfirmTransactionCallback callback){
        //Credentials credentials = getCredential(fromAddress, password);

        values = getLocalContentValues();

        if (mTokenItem != null){  //token transfer
            token20Address  = mTokenItem.getContractAddress();
            int token20Decimals = mTokenItem.getDecimals();
            values.put(DbUtils.TxTableColumns.CONTRACT_ADDRESS, token20Address);
            values.put(DbUtils.TxTableColumns.TOKEN_SYMBOL, mTokenItem.getCoinName());
            try {
                sendTokenTransaction(fromAddress, toAddress, token20Address, credentials, gasPrice, defaultGasLimit, amount, token20Decimals, callback);
            } catch (CipherException e){
                handleErrorCallback(callback, e);
            }
        }else { // normal transfer
            try {
                values.put(DbUtils.TxTableColumns.VALUE, new BigDecimal(amount).multiply(ExchangeCalUtil.ONE_ETHER).toString());
                sendTransaction(fromAddress, toAddress, credentials, gasPrice, gasLimit, amount, extraData, callback);
            } catch (CipherException e) {
                handleErrorCallback(callback, e);
            }
        }
    }

    private void sendTransaction(final String address, final String toAddress, final Credentials credentials,
                                 final String gasPrice, final String gasLimit, final String amount, final String extraData, final ConfirmTransactionCallback callback) throws CipherException{

        try {
            EtherscanAPI.getInstance().getNonceForAddress(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handleErrorCallback(callback, e);
                    Log.e(AppUtils.APP_TAG, "SendEthTransactionAsyncTask sendTransaction onFailure IOException", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        long localNonce = queryNonce();
                        AppUtils.log("localNonce = " + localNonce);

                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String result = jsonObject.getString("result").substring(2);
                        Long netNonce = Long.parseLong(result, 16);
                        AppUtils.log("netNonce = " + netNonce);

                        if (localNonce >= netNonce){
                            netNonce = localNonce + 1;
                        }

                        BigInteger nonce = BigInteger.valueOf(netNonce);
                        AppUtils.log("nonce ready to insert = " + nonce);
                        BigInteger gasP = new BigInteger(gasPrice);
                        values.put(DbUtils.TxTableColumns.NONCE, nonce.toString());
                        RawTransaction tx = getRawTransaction(
                                nonce,
                                gasP,
                                defaultGasLimit,
                                toAddress,
                                new BigDecimal(amount).multiply(ExchangeCalUtil.ONE_ETHER).toBigInteger(),
                                extraData);

                        byte[] signed = TransactionEncoder.signMessage(tx ,(byte) 1 ,credentials);

                        pushTransaction(signed, callback);
                    } catch (Exception e){
                        handleErrorCallback(callback, e);
                        Log.e(AppUtils.APP_TAG, "SendEthTransactionAsyncTask sendTransaction onResponse Exception", e);
                    }
                }
            });
        } catch (IOException e){
            handleErrorCallback(callback, e);
            Log.e(AppUtils.APP_TAG, "SendEthTransactionAsyncTask sendTransaction onResponse IOException", e);
        }
    }

    private void sendTokenTransaction(final String address, final String toAddress, final String contractAddress,
                                      final Credentials credentials, final String gasPrice, final BigInteger gasLimit,
                                      final String amount, final int decimals, final ConfirmTransactionCallback callback) throws CipherException{
        try {
            EtherscanAPI.getInstance().getNonceForAddress(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handleErrorCallback(callback, e);
                    Log.e(AppUtils.APP_TAG, "SendEthTransactionAsyncTask sendTokenTransaction onFailure IOException", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        String result = object.getString("result").substring(2);
                        BigInteger nonce = new BigInteger(result, 16);
                        String extraData = packageTokenData(toAddress, amount, decimals);
                        //transfer amount is in the extra data, not in raw transaction.
                        RawTransaction transaction = getRawTransaction(nonce, new BigInteger(gasPrice), defaultGasLimit, contractAddress, BigInteger.ZERO, extraData);

                        byte[] singedMessage = TransactionEncoder.signMessage(transaction, (byte) 1, credentials);
                        pushTransaction(singedMessage, callback);
                    }catch (JSONException e){
                        handleErrorCallback(callback, e);
                        Log.e(AppUtils.APP_TAG, "SendEthTransactionAsyncTask sendTokenTransaction onResponse JSONException", e);
                    }
                }
            });
        }catch (IOException e){
            handleErrorCallback(callback, e);
            Log.e(AppUtils.APP_TAG, "SendEthTransactionAsyncTask sendTokenTransaction onResponse IOException", e);
        }
    }

    private void pushTransaction(byte[] singedMessage, final ConfirmTransactionCallback callback){
//        values.put(DbUtils.TxTableColumns.TX_HASH, "0000000");
//        Uri uri = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
//                .insert(XWalletProvider.CONTENT_URI_TRANSACTION, values);
//        if (uri != null){
//            handleOkCallback(callback);
//        }else {
//            handleErrorCallback(callback, null);
//        }
//        return;
        try {
            EtherscanAPI.getInstance().forwardTransaction("0x" + Hex.toHexString(singedMessage), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handleErrorCallback(callback, e);
                    Log.e(AppUtils.APP_TAG, "SendEthTransactionAsyncTask pushTransaction onFailure IOException", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String receive = response.body().string();
                    try {
                        JSONObject object = new JSONObject(receive);
                        AppUtils.log("forward tx result: " + object);

                        if (object.has("result")) {
                            String result = object.getString("result");
                            if (result != null && !result.contains("Error")) {
                                values.put(DbUtils.TxTableColumns.TX_HASH, result);
                                Uri uri = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                                        .insert(XWalletProvider.CONTENT_URI_TRANSACTION, values);
                                if (uri != null) {
                                    handleOkCallback(callback);
                                    AppUtils.log("push tx success: " + result);
                                } else {
                                    handleErrorCallback(callback, new Throwable("insert db error"));
                                }
                            } else {
                                if (result != null && result.contains("Error")) {
                                    handleErrorCallback(callback, new Throwable(result));
                                }
                            }
                        }else {
                            String errorMsg = object.getJSONObject("error").getString("message");
                            handleErrorCallback(callback, new Throwable(errorMsg));
                            AppUtils.log("push tx Error: " + errorMsg);
                        }
                    }catch (JSONException e){
                        AppUtils.log(e.toString());
                        handleErrorCallback(callback, e);
                    }
                }
            });
        } catch (IOException e){
            handleErrorCallback(callback, e);
            Log.e(AppUtils.APP_TAG, "SendEthTransactionAsyncTask pushTransaction onResponse IOException", e);
        }
    }

    private void handleOkCallback(ConfirmTransactionCallback callback){
        callback.onTransactionConfirmed(true, null);
    }

    private void handleErrorCallback(ConfirmTransactionCallback callback, Throwable e){
        callback.onTransactionConfirmed(false, e);
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
        values.put(DbUtils.TxTableColumns.VALUE, out.toString());
        values.put(DbUtils.TxTableColumns.TOKEN_DECIMALS, decimals);
        Uint256 value = new Uint256(out);
        inputParameters.add(sendToAddress);
        inputParameters.add(value);

        TypeReference<Bool> typeReference = new TypeReference<Bool>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        return FunctionEncoder.encode(function);
    }

    private ContentValues getLocalContentValues(){
        ContentValues values = new ContentValues();
        values.put(DbUtils.TxTableColumns.TIME_STAMP, System.currentTimeMillis() / 1000);         //2
        // values.put(DbUtils.TxTableColumns.NONCE, bean.getNonce());                  //3
        values.put(DbUtils.TxTableColumns.FROM_ADDRESS, fromAddress);            //4
        values.put(DbUtils.TxTableColumns.TO_ADDRESS, toAddress);                //5
        // values.put(DbUtils.TxTableColumns.VALUE, amount);                  //6
        values.put(DbUtils.TxTableColumns.GAS_LIMIT, defaultGasLimit.toString());                //7
        values.put(DbUtils.TxTableColumns.GAS_PRICE, gasPrice);           //8
        //   values.put(DbUtils.TxTableColumns.INPUT_DATA, bean.getInput());             //11
        //   values.put(DbUtils.TxTableColumns.CONTRACT_ADDRESS, token20Address);     //13
        return values;
    }

    private String queryKeyStore(){
        Cursor cursor = null;
        try {
            cursor = XWalletApplication.getApplication().getContentResolver().query(XWalletProvider.CONTENT_URI,
                    new String[]{DbUtils.DbColumns.KEYSTORE}, DbUtils.ADDRESS_SELECTION, new String[]{address}, null);
            if (cursor != null && cursor.moveToFirst()){
                return cursor.getString(0);
            }
        } finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return null;
    }

    private long queryNonce(){
        long localNonce = 0;
        Cursor cursor = null;
        try {
            final String selection = DbUtils.TxTableColumns.FROM_ADDRESS + " = ?";
            final String sortOrder = DbUtils.TxTableColumns.NONCE + " DESC";
            cursor = XWalletApplication.getApplication().getContentResolver().query(XWalletProvider.CONTENT_URI_TRANSACTION,
                    new String[]{DbUtils.TxTableColumns.NONCE},selection, new String[]{address}, sortOrder);
            if (cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                localNonce = Long.parseLong(cursor.getString(0));
            }

        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return localNonce;
    }
}
