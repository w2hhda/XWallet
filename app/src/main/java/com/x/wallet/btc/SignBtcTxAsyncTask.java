package com.x.wallet.btc;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.x.wallet.lib.btc.CustomeAddress;

import net.bither.bitherj.core.Tx;
import net.bither.bitherj.exception.PasswordException;


/**
 * Created by wuliang on 18-4-12.
 */

public class SignBtcTxAsyncTask extends AsyncTask<Void, Void, Integer>{
    private static final String TAG = "testSign";
    private ProgressDialog mProgressDialog;
    private String mPassword;
    private String mFromAddress;
    private Tx mTx;
    private OnSignBtcTxFinishedListener mOnSignBtcTxFinishedListener;

    public SignBtcTxAsyncTask(Context context, String password, String fromAddress, Tx tx,
                              OnSignBtcTxFinishedListener onSendOutBtcFinishedListener) {
        mProgressDialog = new ProgressDialog(context);
        mPassword = password;
        mFromAddress = fromAddress;
        mTx = tx;
        mOnSignBtcTxFinishedListener = onSendOutBtcFinishedListener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        return signTx();
    }

    @Override
    protected void onPostExecute(Integer result) {
        mProgressDialog.dismiss();
        if(mOnSignBtcTxFinishedListener != null){
            mOnSignBtcTxFinishedListener.onSignBtcTxFinished(result);
        }
    }

    private int signTx() {
        try {
            CustomeAddress.signTx(mFromAddress, mTx, mPassword);
            if (!mTx.verifySignatures()) {
                Log.e(TAG, "SendOutBtcAsyncTask signTx verifySignatures fail!");
                return SendOutTxResult.ERROR_UNKNOWN;
            }
            return SendOutTxResult.RESULT_OK;
        } catch (Exception e) {
            if(e instanceof PasswordException){
                return SendOutTxResult.ERROR_PASSWORD_WRONG;
            }
            Log.e(TAG, "SendOutBtcAsyncTask signTx Exception", e);
        }
        return SendOutTxResult.ERROR_UNKNOWN;
    }

    public interface OnSignBtcTxFinishedListener {
        void onSignBtcTxFinished(int resultCode);
    }

    public interface SendOutTxResult{
        int RESULT_OK            = -1;
        int ERROR_UNKNOWN        = 0;
        int ERROR_PASSWORD_WRONG = 1;
    }
}
