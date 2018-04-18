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
    private String mAccountAddress;
    private boolean mHasToken;
    private TokenItemBean mTokenItem;
    private ProgressDialog mProgressDialog;
    private OnInsertTokenFinishedListener mOnInsertTokenFinishedListener;

    public InsertTokenAsycTask(Context context, long accountId, String accountAddress, boolean hasToken,
                               TokenItemBean tokenItem, OnInsertTokenFinishedListener listener) {
        mAccountId = accountId;
        mAccountAddress = accountAddress;
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
        boolean isExist = DbUtils.isAlreadyExistToken(DbUtils.UPDATE_TOKEN_SELECTION, new String[]{mAccountAddress, mTokenItem.getSymbol()});
        Log.i(AppUtils.APP_TAG, "doInBackground isExist = " + isExist);
        if(isExist){
            return null;
        }

        ContentValues values = new ContentValues();
        values.put(DbUtils.TokenTableColumns.ACCOUNT_ID, mAccountId);
        values.put(DbUtils.TokenTableColumns.ACCOUNT_ADDRESS, mAccountAddress);
        values.put(DbUtils.TokenTableColumns.ID_IN_ALL, mTokenItem.getIdInAll());
        values.put(DbUtils.TokenTableColumns.NAME, mTokenItem.getName());
        values.put(DbUtils.TokenTableColumns.SYMBOL, mTokenItem.getSymbol());
        values.put(DbUtils.TokenTableColumns.DECIMALS, mTokenItem.getDecimals());
        values.put(DbUtils.TokenTableColumns.CONTRACT_ADDRESS, mTokenItem.getContractAddress());
        values.put(DbUtils.TokenTableColumns.BALANCE, "0");
        values.put(DbUtils.TokenTableColumns.RATE, "0");
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
