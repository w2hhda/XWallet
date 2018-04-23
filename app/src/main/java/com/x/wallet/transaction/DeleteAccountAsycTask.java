package com.x.wallet.transaction;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.token.DeleteTokenHelper;

/**
 * Created by wuliang on 18-3-15.
 */

public class DeleteAccountAsycTask extends AsyncTask<Void, Void, Integer>{
    private long mAccountId;
    private String mAddress;
    private int mCoinType;
    private OnDeleteFinishedListener mOnDeleteFinishedListener;

    private ProgressDialog mProgressDialog;

    public DeleteAccountAsycTask(Context context, long accountId, String address, int coinType, OnDeleteFinishedListener listener) {
        mAccountId =accountId;
        mAddress = address;
        mCoinType = coinType;
        mProgressDialog = new ProgressDialog(context);
        mOnDeleteFinishedListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        DbUtils.deleteTokenForAccount(mAccountId);
        if(mCoinType == LibUtils.COINTYPE.COIN_ETH){
            DeleteTokenHelper.removeSharePref(mAddress);
        }
        Uri uri = ContentUris.withAppendedId(XWalletProvider.CONTENT_URI, mAccountId);
        return XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                .delete(uri, null, null);
    }

    @Override
    protected void onPostExecute(Integer count) {
        mProgressDialog.dismiss();
        if(mOnDeleteFinishedListener != null){
            mOnDeleteFinishedListener.onDeleteFinished(count);
        }
    }

    public interface OnDeleteFinishedListener{
        void onDeleteFinished(int count);
    }
}
