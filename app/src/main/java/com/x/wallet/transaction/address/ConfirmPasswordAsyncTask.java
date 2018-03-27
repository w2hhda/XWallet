package com.x.wallet.transaction.address;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.api.EtherscanAPI;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Nick on 26/3/2018.
 */

public class ConfirmPasswordAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private Context mContext;
    private Intent intent;
    private String password;
    private String address;

    private ProgressDialog mProgressDialog;


    public ConfirmPasswordAsyncTask(Context context, Intent intent, String passwrod, String address){
        mContext = context;
        this.password = passwrod;
        this.address = address;
        this.intent = intent;
        mProgressDialog = new ProgressDialog(context);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        String keyStore;
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
                    ECKeyPair keyPair = Wallet.decrypt(password, walletFile);
                    if (keyPair != null){
                        WalletFile file = Wallet.createStandard(password, keyPair);
                        String newAddress = "0x" +file.getAddress();
                        if (newAddress.equalsIgnoreCase(address)){
                            Log.i("@@@@","true");

                            return true;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }catch (CipherException e){

                }
            }

        }
        return false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.show();

    }

    @Override
    protected void onPostExecute(Boolean s) {
        super.onPostExecute(s);
        mProgressDialog.dismiss();
        if (s){
            mContext.startService(intent);
        }else {
            Toast.makeText(mContext, "error password", Toast.LENGTH_SHORT).show();
        }

    }
}
