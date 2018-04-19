package com.x.wallet.transaction.key;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.lib.eth.api.EthAccountCreateHelper;

import net.bither.bitherj.core.BtcCreateAddressHelper;

/**
 * Created by wuliang on 18-3-15.
 */

public class DecryptKeyAsycTask extends AsyncTask<Void, Void, String>{
    private int mCoinType;
    private String mPassword;

    private String mKeyStore;

    private String mEncryptHdSeed;
    private String mPrivKey;

    private ProgressDialog mProgressDialog;
    private Context mContext;

    private OnDecryptKeyFinishedListener mOnDecryptKeyFinishedListener;

    public DecryptKeyAsycTask(Context context, int coinType, String password, String keyStore,
                              String encryptHdSeed, String privKey, OnDecryptKeyFinishedListener listener) {
        mProgressDialog = new ProgressDialog(context);
        mContext = context;
        mCoinType = coinType;
        mPassword = password;
        mKeyStore = keyStore;
        mEncryptHdSeed = encryptHdSeed;
        mPrivKey = privKey;
        mOnDecryptKeyFinishedListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        switch (mCoinType){
            case LibUtils.COINTYPE.COIN_ETH:
                return EthAccountCreateHelper.restoreKeyFromKeyStore(mKeyStore, mPassword);
            case LibUtils.COINTYPE.COIN_BTC:
                return BtcCreateAddressHelper.readPrivateKey(mPrivKey, mEncryptHdSeed, mPassword);
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
