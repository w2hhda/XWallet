package com.x.wallet.btc;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.bither.bitherj.api.PushTxThirdParty;
import net.bither.bitherj.core.PeerManager;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.core.UnSignTransaction;
import net.bither.bitherj.utils.TransactionsUtil;


/**
 * Created by wuliang on 18-4-12.
 */

public class PushBtcAsyncTask extends AsyncTask<Void, Void, Integer>{
    private static final String TAG = "testPush";
    private ProgressDialog mProgressDialog;
    private String mFromAddress;
    private Tx mTx;
    private OnPushBtcTxFinishedListener mOnPushBtcTxFinishedListener;

    public PushBtcAsyncTask(Context context, String fromAddress, Tx tx,
                            OnPushBtcTxFinishedListener onPushBtcTxFinishedListener) {
        mProgressDialog = new ProgressDialog(context);
        mFromAddress = fromAddress;
        mTx = tx;
        mOnPushBtcTxFinishedListener = onPushBtcTxFinishedListener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try{
            PushTxThirdParty.getInstance().pushTx(mTx);
            PeerManager.instance().publishTransaction(mTx);
            TransactionsUtil.removeSignTx(new UnSignTransaction(mTx, mFromAddress));
        } catch (Exception e){
            Log.e(TAG, "PushBtcAsyncTask doInBackground Exception", e);
            return PushOutTxResult.ERROR_UNKNOWN;
        }
        return PushOutTxResult.RESULT_OK;
    }

    @Override
    protected void onPostExecute(Integer result) {
        mProgressDialog.dismiss();
        if(mOnPushBtcTxFinishedListener != null){
            mOnPushBtcTxFinishedListener.onPushBtcTxFinished(result);
        }
    }

    public interface OnPushBtcTxFinishedListener {
        void onPushBtcTxFinished(int resultCode);
    }

    public interface PushOutTxResult {
        int RESULT_OK            = -1;
        int ERROR_UNKNOWN        = 0;
    }
}
