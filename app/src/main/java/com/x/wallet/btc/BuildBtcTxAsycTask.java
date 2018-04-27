package com.x.wallet.btc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.x.wallet.R;
import com.x.wallet.lib.btc.CustomeAddress;
import com.x.wallet.lib.btc.TxBuildResult;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.ui.dialog.ContentShowDialogHelper;
import com.x.wallet.ui.dialog.PasswordCheckDialogHelper;

import net.bither.bitherj.core.Tx;

/**
 * Created by wuliang on 18-3-15.
 */

public class BuildBtcTxAsycTask extends AsyncTask<Void, Void, TxBuildResult>{
    private Context mContext;
    private long mAmount;
    private String mFromAddress;
    private String mToAddress;
    private String mChangeAddress;
    private int mFeeBase;

    private ProgressDialog mProgressDialog;
    private OnTxBuildFinishedListener mOnTxBuildFinishedListener;

    public BuildBtcTxAsycTask(Context context, long amount, String fromAddress, String toAddress,
                              String changeAddress, int feeBase, OnTxBuildFinishedListener onTxBuildFinishedListener) {
        mProgressDialog = new ProgressDialog(context);
        mContext = context;
        mAmount = amount;
        mFromAddress = fromAddress;
        mToAddress = toAddress;
        mChangeAddress = changeAddress;
        mFeeBase = feeBase;
        mOnTxBuildFinishedListener = onTxBuildFinishedListener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected TxBuildResult doInBackground(Void... voids) {
        return CustomeAddress.buildTx(mAmount, mFromAddress, mToAddress, mChangeAddress, mFeeBase);
    }

    @Override
    protected void onPostExecute(TxBuildResult txBuildResult) {
        mProgressDialog.dismiss();
        //Log.i("testBtcTx", "BuildBtcTxAsycTask onPostExecute mResultCode = " + txBuildResult.mResultCode);
        //Log.i("testBtcTx", "              ");
        //Log.i("testBtcTx", "              ");
        if(txBuildResult.mTx == null){
            if(mOnTxBuildFinishedListener != null){
                mOnTxBuildFinishedListener.onTxBuildFinished(txBuildResult);
            }
        } else {
            showPasswordCheckDialog(txBuildResult.mTx);
        }
    }

    private void showPasswordCheckDialog(final Tx tx){
        final PasswordCheckDialogHelper passwordCheckDialogHelper = new PasswordCheckDialogHelper();

        passwordCheckDialogHelper.showPasswordDialog(mContext, new PasswordCheckDialogHelper.ConfirmBtnClickListener() {
            @Override
            public void onConfirmBtnClick(String password, Context context) {
                new SignBtcTxAsyncTask(mContext, password, mFromAddress, tx, new SignBtcTxAsyncTask.OnSignBtcTxFinishedListener() {
                    @Override
                    public void onSignBtcTxFinished(int resultCode) {
                        //Log.i("test", "BuildBtcTxAsycTask showPasswordCheckDialog resultCode = " + resultCode);
                        switch (resultCode){
                            case SignBtcTxAsyncTask.SendOutTxResult.RESULT_OK:
                                passwordCheckDialogHelper.dismissDialog();
                                ContentShowDialogHelper.showConfirmDialog(mContext, R.string.confirm_transaction
                                        , buildTxConfirmText(tx)
                                        ,null
                                        ,new View.OnClickListener(){
                                            @Override
                                            public void onClick(View view) {
                                                new PushBtcAsyncTask(mContext, mFromAddress, tx, new PushBtcAsyncTask.OnPushBtcTxFinishedListener() {
                                                    @Override
                                                    public void onPushBtcTxFinished(int resultCode) {
                                                        //Log.i("testBtcTx", "BuildBtcTxAsycTask showPasswordCheckDialog PushBtcAsyncTask resultCode = " + resultCode);
                                                        if(resultCode == PushBtcAsyncTask.PushOutTxResult.RESULT_OK){
                                                            Toast.makeText(mContext, R.string.send_out_btc_transaction_success, Toast.LENGTH_LONG).show();
                                                            if(mOnTxBuildFinishedListener != null){
                                                                mOnTxBuildFinishedListener.onTxPushFinished(resultCode);
                                                            }
                                                        } else {
                                                            Toast.makeText(mContext, R.string.send_out_btc_transaction_failed, Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                }).execute();
                                            }
                                        });
                                break;
                            case SignBtcTxAsyncTask.SendOutTxResult.ERROR_PASSWORD_WRONG:
                                passwordCheckDialogHelper.updatePasswordCheckError();
                                break;
                            case SignBtcTxAsyncTask.SendOutTxResult.ERROR_UNKNOWN:
                                Toast.makeText(mContext, R.string.sign_btc_tx_failed, Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                }).execute();
            }
        }, R.string.confirm_password);
    }


    private String buildTxConfirmText(Tx tx){
        return mContext.getString(R.string.send_confirm_address) + mToAddress + "\n" +
        mContext.getString(R.string.send_confirm_amount) + TokenUtils.translate(tx.amountSentToAddress(mToAddress), BtcUtils.BTC_DECIMALS_COUNT) + " BTC\n" +
                mContext.getString(R.string.send_confirm_fee) + TokenUtils.translate(tx.getFee(), BtcUtils.BTC_DECIMALS_COUNT) + " BTC";
    }

    public interface OnTxBuildFinishedListener{
        void onTxBuildFinished(TxBuildResult txBuildResult);
        void onTxPushFinished(int resultCode);
    }
}
