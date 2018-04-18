package com.x.wallet.transaction.usdtocny;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.x.wallet.btc.BtcUtils;
import com.x.wallet.transaction.balance.RetrofitClient;

/**
 * Created by wuliang on 18-3-15.
 */

public class ChangeCurrencyAsycTask extends AsyncTask<Void, Void, Double>{
    private String mChooseCurrency;

    private ProgressDialog mProgressDialog;
    private Context mContext;
    private OnChangeFinishedListener mOnChangeFinishedListener;

    public ChangeCurrencyAsycTask(Context context, String chooseCurrency, OnChangeFinishedListener listener) {
        mChooseCurrency = chooseCurrency;
        mProgressDialog = new ProgressDialog(context);
        mContext = context;
        mOnChangeFinishedListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Double doInBackground(Void... voids) {
        double result = UsdToCnyHelper.requestCurrencyToUsd(mChooseCurrency);
        if(result > 0){
            UsdToCnyHelper.write(result);
            RetrofitClient.requestBalance(null, false);
        }
        BtcUtils.requestCurrencyBtcPrice(mChooseCurrency);
        return result;
    }

    @Override
    protected void onPostExecute(Double count) {
        mProgressDialog.dismiss();
        if(mOnChangeFinishedListener != null){
            mOnChangeFinishedListener.onChangeFinished(count);
        }
    }

    public interface OnChangeFinishedListener{
        void onChangeFinished(Double result);
    }
}
