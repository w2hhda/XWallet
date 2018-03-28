package com.x.wallet.transaction.address;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.MainActivity;
import com.x.wallet.R;
import com.x.wallet.lib.common.LibUtils;

/**
 * Created by wuliang on 18-3-15.
 */

public class ImportAddressAsycTask extends AsyncTask<Void, Void, Uri>{
    private int mCoinType;
    private int mImportType;
    private String mPassword;
    private String mAccountName;

    private int mMnemonicType;
    private String mMnemonic;

    private String mKey;

    private String mKeyStore;
    private String mKeyStorePassword;

    private ProgressDialog mProgressDialog;
    private Context mContext;

    public ImportAddressAsycTask(Context context, int importType, int coinType, String password, String accountName) {
        mContext = context;
        mCoinType = coinType;
        mImportType = importType;
        mPassword = password;
        mAccountName = accountName;
        mProgressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Uri doInBackground(Void... voids) {
        switch (mImportType){
            case AppUtils.IMPORTTYPE.IMPORT_TYPE_MNEMONIC:
                return AddressUtils.importAddressThroughMnemonic(mCoinType,
                        mPassword,
                        mAccountName,
                        mMnemonicType,
                        mMnemonic);
            case AppUtils.IMPORTTYPE.IMPORT_TYPE_KEY:
                return AddressUtils.importAddressThroughKey(mCoinType,
                        mPassword,
                        mAccountName,
                        mKey);
            case AppUtils.IMPORTTYPE.IMPORT_TYPE_KEYSTORE:
                return AddressUtils.importAddressThroughKeyStore(mCoinType,
                        mPassword,
                        mAccountName, mKeyStore, mKeyStorePassword);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Uri uri) {
        mProgressDialog.dismiss();
        if(uri != null){
            Toast.makeText(mContext, R.string.import_address_success, Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(mContext, R.string.import_address_failed, Toast.LENGTH_LONG).show();
        }
        if(mContext instanceof Activity){
            ((Activity) mContext).finish();
        }
    }

    public void setMnemonic(String mnemonic) {
        this.mMnemonic = mnemonic;
    }

    public void setKeyStore(String keyStore, String keyStorePassword) {
        mKeyStore = keyStore;
        mKeyStorePassword = keyStorePassword;
    }

    public void setKey(String key) {
        mKey = key;
    }
}
