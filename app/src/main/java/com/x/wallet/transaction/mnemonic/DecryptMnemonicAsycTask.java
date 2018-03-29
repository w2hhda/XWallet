package com.x.wallet.transaction.mnemonic;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;

import net.bither.bitherj.core.BtcCreateAddressHelper;

import java.util.List;

/**
 * Created by wuliang on 18-3-27.
 */

public class DecryptMnemonicAsycTask extends AsyncTask<Void, Void, List<String>> {

    private Uri mUri;
    private String mPassword;
    private ProgressDialog mProgressDialog;

    private OnDecryptMnemonicFinishedListener mOnDecryptMnemonicFinishedListener;

    public DecryptMnemonicAsycTask(Context context, Uri uri, String password, OnDecryptMnemonicFinishedListener onDecryptMnemonicFinishedListener) {
        mUri = uri;
        mPassword = password;
        mProgressDialog = new ProgressDialog(context);
        mOnDecryptMnemonicFinishedListener = onDecryptMnemonicFinishedListener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        Cursor cursor = null;
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                .query(mUri, new String[]{DbUtils.DbColumns.ENCRYPT_MNEMONIC}, null, null, null);
            if(cursor != null){
                if(cursor.moveToFirst()){
                    String encryptMnemonic = cursor.getString(0);
                    if(!TextUtils.isEmpty(encryptMnemonic)){
                        return BtcCreateAddressHelper.readMnemonicToList(encryptMnemonic, mPassword);
                    }
                }
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<String> words) {
        mProgressDialog.dismiss();
        if(mOnDecryptMnemonicFinishedListener != null){
            mOnDecryptMnemonicFinishedListener.onDecryptMnemonicFinished(words);
        }
    }

    public interface OnDecryptMnemonicFinishedListener{
        void onDecryptMnemonicFinished(List<String> result);
    }
}
