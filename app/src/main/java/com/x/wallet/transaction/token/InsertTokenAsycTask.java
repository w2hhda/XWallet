package com.x.wallet.transaction.token;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.ui.data.TokenItemBean;

/**
 * Created by wuliang on 18-3-27.
 */

public class InsertTokenAsycTask extends AsyncTask<Void, Void, Uri> {
    private long mAccountId;
    private boolean mHasToken;
    private TokenItemBean mTokenItem;
    private ProgressDialog mProgressDialog;
    private OnInsertTokenFinishedListener mOnInsertTokenFinishedListener;

    public InsertTokenAsycTask(Context context, long accountId, boolean hasToken,
                               TokenItemBean tokenItem, OnInsertTokenFinishedListener listener) {
        mAccountId = accountId;
        mHasToken = hasToken;
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
        ContentValues values = new ContentValues();
        values.put(DbUtils.TokenTableColumns.ACCOUNT_ID, mAccountId);
        values.put(DbUtils.TokenTableColumns.ID_IN_ALL, mTokenItem.getId());
        values.put(DbUtils.TokenTableColumns.ADDRESS, mTokenItem.getAddress());
        values.put(DbUtils.TokenTableColumns.SHORT_NAME, mTokenItem.getShortname());
        values.put(DbUtils.TokenTableColumns.WHOLE_NAME, mTokenItem.getWholename());
        values.put(DbUtils.TokenTableColumns.BALANCE, mTokenItem.getBalance());
        Uri uri = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                .insert(XWalletProvider.CONTENT_URI_TOKEN, values);

        if (!mHasToken) {
            ContentValues updateValues = new ContentValues();
            updateValues.put(DbUtils.DbColumns.HAS_TOKEN, AppUtils.HAS_TOKEN);
            int count = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                    .update(XWalletProvider.CONTENT_URI, updateValues,
                            DbUtils.DbColumns._ID + " = ?",
                            new String[]{String.valueOf(mAccountId)});
            Log.i(AppUtils.APP_TAG, "InsertTokenAsycTask doInBackground count = " + count + ", mAccountId = " + mAccountId);
        }
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
