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
import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.ui.activity.TransferActivity;
import com.x.wallet.ui.data.RawAccountItem;
import com.x.wallet.ui.data.SerializableAccountItem;

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
        String selection = DbUtils.DbColumns.ADDRESS + " = ?";
        Cursor cursor = null;
        try {
            cursor = XWalletApplication.getApplication().getContentResolver().query(XWalletProvider.CONTENT_URI,
                    new String[]{DbUtils.DbColumns.KEYSTORE},selection, new String[]{address}, null);

            if (cursor != null && cursor.moveToFirst()){
                keyStore = cursor.getString(0);
                if (keyStore == null){
                    return  false;
                }

                ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

                try {
                    WalletFile walletFile = mapper.readValue(keyStore, WalletFile.class);
                    ECKeyPair keyPair = Wallet.decrypt(password, walletFile);
                    if (keyPair != null){
                        WalletFile file = Wallet.createStandard(password, keyPair);
                        String newAddress = "0x" +file.getAddress();
                        if (newAddress.equalsIgnoreCase(address)){
                            return true;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }catch (CipherException e){

                }
            }
        } finally {
            if (cursor != null){
                cursor.close();
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
            SerializableAccountItem accountItem = (SerializableAccountItem) intent.getSerializableExtra(AppUtils.ACCOUNT_DATA);
            Intent newIntent = new Intent("com.x.wallet.action.SEE_ACCOUNT_DETAIL_ACTION");
            newIntent.putExtra(AppUtils.ACCOUNT_DATA, accountItem);
            if (intent.hasExtra(AppUtils.TOKEN_DATA)){
                RawAccountItem mTokenItem = (RawAccountItem)intent.getSerializableExtra(AppUtils.TOKEN_DATA);
                newIntent.putExtra(AppUtils.TOKEN_DATA, mTokenItem);
            }
            newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(newIntent);

        }else {
            Toast.makeText(mContext, "error password", Toast.LENGTH_SHORT).show();
        }

    }
}
