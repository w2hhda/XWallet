package com.x.wallet.transaction;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;

/**
 * Created by wuliang on 18-3-15.
 */

public class DeleteAccountAsycTask extends AsyncTask<Void, Void, Integer>{
    private long mAccountId;

    private ProgressDialog mProgressDialog;
    private Context mContext;
    private OnDeleteFinishedListener mOnDeleteFinishedListener;

    public DeleteAccountAsycTask(Context context, long accountId, OnDeleteFinishedListener listener) {
        mAccountId =accountId;
        mProgressDialog = new ProgressDialog(context);
        mContext = context;
        mOnDeleteFinishedListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        int count = DbUtils.deleteTokenForAccount(mAccountId);
        Log.i(AppUtils.APP_TAG, "DeleteAccountAsycTask doInBackground count = " + count);
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
