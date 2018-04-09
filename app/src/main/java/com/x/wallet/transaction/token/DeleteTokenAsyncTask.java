package com.x.wallet.transaction.token;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.ui.data.TokenItem;

public class DeleteTokenAsyncTask extends AsyncTask<Void, Void, Integer> {
    private TokenItem tokenItem;
    private OnDeleteTokenFinishedListener listener;
    private ProgressDialog mProgressDialog;
    private String address, contractAddress;

    public DeleteTokenAsyncTask(Context context, TokenItem tokenItem, String address, OnDeleteTokenFinishedListener listener) {
        super();
        this.tokenItem = tokenItem;
        this.listener = listener;
        this.address = address;
        contractAddress = tokenItem.getContractAddress();
        mProgressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        String selection = DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " = ? AND " + DbUtils.TokenTableColumns.CONTRACT_ADDRESS + " = ?";
        return XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                .delete(XWalletProvider.CONTENT_URI_TOKEN, selection, new String[]{address, contractAddress});

    }

    @Override
    protected void onPostExecute(Integer i) {
        super.onPostExecute(i);
        mProgressDialog.dismiss();
        if (listener != null){
            listener.onDeleteFinished();
        }
    }

    public interface OnDeleteTokenFinishedListener {
        void onDeleteFinished();
    }
}
