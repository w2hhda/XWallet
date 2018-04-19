package com.x.wallet.transaction.token;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.db.DbUtils;
import com.x.wallet.ui.data.TokenItemBean;

/**
 * Created by wuliang on 18-3-27.
 */

public class InsertTokenAsycTask extends AsyncTask<Void, Void, Uri> {
    private long mAccountId;
    private String mAccountAddress;
    private TokenItemBean mTokenItem;
    private ProgressDialog mProgressDialog;
    private OnInsertTokenFinishedListener mOnInsertTokenFinishedListener;

    public InsertTokenAsycTask(Context context, long accountId, String accountAddress,
                               TokenItemBean tokenItem, OnInsertTokenFinishedListener listener) {
        mAccountId = accountId;
        mAccountAddress = accountAddress;
        mTokenItem = tokenItem;
        mProgressDialog = new ProgressDialog(context);
        mOnInsertTokenFinishedListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Uri doInBackground(Void... voids) {
        boolean isExist = DbUtils.isAlreadyExistToken(DbUtils.UPDATE_TOKEN_SELECTION, new String[]{mAccountAddress, mTokenItem.getSymbol()});
        Log.i(AppUtils.APP_TAG, "doInBackground isExist = " + isExist);
        if(isExist){
            return null;
        }

        Uri uri = DbUtils.insertTokenIntoDb(mAccountId, mAccountAddress, mTokenItem.getIdInAll(),
                mTokenItem.getName(), mTokenItem.getSymbol(), mTokenItem.getDecimals(),
                mTokenItem.getContractAddress(), "0", "0");

        Log.i(AppUtils.APP_TAG, "InsertTokenAsycTask doInBackground uri = " + uri);
        return uri;
    }

    @Override
    protected void onPostExecute(Uri uri) {
        mProgressDialog.dismiss();
        if (mOnInsertTokenFinishedListener != null) {
            mOnInsertTokenFinishedListener.onInsertFinished();
        }
    }

    public interface OnInsertTokenFinishedListener {
        void onInsertFinished();
    }
}
