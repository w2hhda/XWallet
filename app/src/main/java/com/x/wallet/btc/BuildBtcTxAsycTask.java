package com.x.wallet.btc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.x.wallet.R;
import com.x.wallet.lib.btc.CustomeAddress;
import com.x.wallet.lib.btc.TxBuildResult;
import com.x.wallet.ui.dialog.ContentShowDialogHelper;
import com.x.wallet.ui.dialog.PasswordCheckDialogHelper;

import net.bither.bitherj.core.Tx;

/**
 * Created by wuliang on 18-3-15.
 */

public class BuildBtcTxAsycTask extends AsyncTask<Void, Void, Tx>{
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
    protected Tx doInBackground(Void... voids) {
        TxBuildResult txBuildResult = CustomeAddress.buildTx(mAmount, mFromAddress, mToAddress, mChangeAddress, mFeeBase);
        return txBuildResult.mTx;
    }

    @Override
    protected void onPostExecute(Tx tx) {
        mProgressDialog.dismiss();
        //Log.i("test", "BuildBtcTxAsycTask onPostExecute tx = " + tx);
        if(tx == null){
            if(mOnTxBuildFinishedListener != null){
                mOnTxBuildFinishedListener.onTxBuildFinished(tx);
            }
        } else {
            showPasswordCheckDialog(tx);
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
                                ContentShowDialogHelper.showConfirmDialog(mContext, R.string.delete_account
                                        , buildTxConfirmText(tx)
                                        , new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                                new PushBtcAsyncTask(mContext, mFromAddress, tx, new PushBtcAsyncTask.OnPushBtcTxFinishedListener() {
                                                    @Override
                                                    public void onPushBtcTxFinished(int resultCode) {
                                                        Log.i("test", "BuildBtcTxAsycTask showPasswordCheckDialog PushBtcAsyncTask resultCode = " + resultCode);
                                                        if(resultCode == PushBtcAsyncTask.PushOutTxResult.RESULT_OK){
                                                            Toast.makeText(mContext, R.string.send_out_btc_transaction_success, Toast.LENGTH_LONG).show();
                                                        } else {
                                                            Toast.makeText(mContext, R.string.sign_btc_tx_failed, Toast.LENGTH_LONG).show();
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
        String outBtcText = Long.toString(tx.amountSentToAddress(mToAddress));
        String feeText = Long.toString(tx.getFee());
        return "Btc: " + outBtcText + "\n" + "Fee:  " + feeText;
    }

    public interface OnTxBuildFinishedListener{
        void onTxBuildFinished(Tx tx);
    }
}
