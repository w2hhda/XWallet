package com.x.wallet.transaction.key;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.lib.eth.api.EthAccountCreateHelper;

/**
 * Created by wuliang on 18-3-15.
 */

public class DecryptKeyAsycTask extends AsyncTask<Void, Void, String>{
    private int mCoinType;
    private String mPassword;
    private String mKey;
    private String mKeyStore;

    private ProgressDialog mProgressDialog;
    private Context mContext;

    private OnDecryptKeyFinishedListener mOnDecryptKeyFinishedListener;

    public DecryptKeyAsycTask(Context context, int coinType, String key, String keyStore, String password, OnDecryptKeyFinishedListener listener) {
        mCoinType = coinType;
        mKey = key;
        mKeyStore = keyStore;
        mPassword = password;
        mProgressDialog = new ProgressDialog(context);
        mContext = context;
        mOnDecryptKeyFinishedListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        if(mCoinType == LibUtils.COINTYPE.COIN_ETH){
            return EthAccountCreateHelper.restoreKeyFromKeyStore(mKeyStore, mPassword);
        }
        return "";
    }

    @Override
    protected void onPostExecute(String key) {
        mProgressDialog.dismiss();
        if(mOnDecryptKeyFinishedListener != null){
            mOnDecryptKeyFinishedListener.onDecryptKeyFinished(key);
        }
    }

    public interface OnDecryptKeyFinishedListener{
        void onDecryptKeyFinished(String result);
    }
}
