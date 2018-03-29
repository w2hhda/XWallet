package com.x.wallet.transaction.keystore;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.lib.eth.api.EthAccountCreateHelper;

/**
 * Created by wuliang on 18-3-15.
 */

public class DecryptKeyStoreAsycTask extends AsyncTask<Void, Void, String>{
    private int mCoinType;
    private String mPassword;
    private String mKeyStore;

    private ProgressDialog mProgressDialog;
    private Context mContext;
    private OnDecryptKeyStoreFinishedListener mOnDecryptKeyStoreFinishedListener;

    public DecryptKeyStoreAsycTask(Context context, int coinType, String keyStore, String password, OnDecryptKeyStoreFinishedListener listener) {
        mCoinType = coinType;
        mKeyStore = keyStore;
        mPassword = password;
        mProgressDialog = new ProgressDialog(context);
        mContext = context;
        mOnDecryptKeyStoreFinishedListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        if(mCoinType == LibUtils.COINTYPE.COIN_ETH){
            return EthAccountCreateHelper.checkPasswordForKeyStore(mKeyStore, mPassword);
        }
        return "";
    }

    @Override
    protected void onPostExecute(String keyStore) {
        mProgressDialog.dismiss();
        if(mOnDecryptKeyStoreFinishedListener != null){
            mOnDecryptKeyStoreFinishedListener.onDecryptKeyStoreFinished(keyStore);
        }
    }

    public interface OnDecryptKeyStoreFinishedListener{
        void onDecryptKeyStoreFinished(String result);
    }
}
