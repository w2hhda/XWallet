package com.x.wallet.btc;

import android.app.LoaderManager;
import android.content.Context;
import android.util.Log;

import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.transaction.token.TokenUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by wuliang on 18-4-16.
 * REQUEST_FEE_TESTNET_URL:
 * String REQUEST_FEE_TESTNET_URL = "https://api.blockcypher.com/v1/btc/test3";
 * {
 "name": "BTC.test3",
 "height": 1293147,
 "hash": "000000000000025767eec04e8fa068268d0f7ba685e713c2ccd78be728baabe2",
 "time": "2018-04-16T13:30:18.936815787Z",
 "latest_url": "https://api.blockcypher.com/v1/btc/test3/blocks/000000000000025767eec04e8fa068268d0f7ba685e713c2ccd78be728baabe2",
 "previous_hash": "00000000000002c94a9d5c5c37c4632741e93c838096eda37eb2bfd64d1cc0f1",
 "previous_url": "https://api.blockcypher.com/v1/btc/test3/blocks/00000000000002c94a9d5c5c37c4632741e93c838096eda37eb2bfd64d1cc0f1",
 "peer_count": 274,
 "unconfirmed_count": 108,
 "high_fee_per_kb": 224141,
 "medium_fee_per_kb": 10000,
 "low_fee_per_kb": 7000,
 "last_fork_height": 1292576,
 "last_fork_hash": "000000009766c4b55f1d6777df70c39155d179dc836045fa9272bcd2188baa51"
 }
 REQUEST_FEE_URL:
 {"fastestFee":30,"halfHourFee":30,"hourFee":10}
 */

public class BtcTransferHelper {
    private static final String TAG = "testGetFee";
    private int mLowFee = 0;
    private int mMiddleFee = 0;
    private int mHighFee = 0;
    private int mTransactionSize = 226;
    private BtcAccountBalanceLoaderHelper mBtcAccountBalanceLoaderHelper;
    private OnTransactionFeeRequestFinishedListener mOnTransactionFeeRequestFinishedListener;

    public BtcTransferHelper(OnTransactionFeeRequestFinishedListener onTransactionFeeRequestFinishedListener) {
        mOnTransactionFeeRequestFinishedListener = onTransactionFeeRequestFinishedListener;
    }

    public void getTransactionFee(){
        String REQUEST_FEE_URL = "https://bitcoinfees.earn.com/api/v1/fees/recommended";
        try {
            EtherscanAPI.getInstance().get(REQUEST_FEE_URL, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.w(TAG, "BtcTransferHelper getTransactionFee", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        ResponseBody body = response.body();
                        if(body != null){
                            String result = body.string();
                            Log.e(TAG, "BtcTransferHelper getTransactionFee result = " + result);
                            JSONObject object = new JSONObject(result);
                            mLowFee = object.getInt("hourFee");
                            mMiddleFee = object.getInt("halfHourFee");
                            mHighFee = object.getInt("fastestFee");
                            if(mOnTransactionFeeRequestFinishedListener != null){
                                mOnTransactionFeeRequestFinishedListener.onFeeRequestFinished(TokenUtils.getBalanceText(mMiddleFee * mTransactionSize, BtcUtils.BTC_DECIMALS_COUNT));
                            }
                        } else {
                            Log.e(TAG, "BtcTransferHelper getTransactionFee body is null!");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "BtcTransferHelper getTransactionFee JSONException", e);
                    }
                }
            });
        } catch (Exception e){
            Log.e(TAG, "BtcTransferHelper getTransactionFee Exception", e);
        }
    }

    public void loadBalance(Context context, LoaderManager loaderManager, String address, BtcAccountBalanceLoaderHelper.OnDataLoadFinishedListener listener){
        mBtcAccountBalanceLoaderHelper = new BtcAccountBalanceLoaderHelper(context, loaderManager, address, listener);
        mBtcAccountBalanceLoaderHelper.forceLoad();
    }

    public void updateGasPrice(final int progress) {
        String price;
        if (progress == 0) {
            price = TokenUtils.getBalanceText(mLowFee * mTransactionSize, BtcUtils.BTC_DECIMALS_COUNT);
        } else if (progress == 100) {
            price = TokenUtils.getBalanceText(mHighFee * mTransactionSize, BtcUtils.BTC_DECIMALS_COUNT);
        } else {
            price = TokenUtils.getBalanceText(mMiddleFee * mTransactionSize, BtcUtils.BTC_DECIMALS_COUNT);
        }
        if (mOnTransactionFeeRequestFinishedListener != null) {
            mOnTransactionFeeRequestFinishedListener.onFeeRequestFinished(price);
        }
    }


    public void destory(){
        if(mBtcAccountBalanceLoaderHelper != null){
            mBtcAccountBalanceLoaderHelper.destory();
        }
    }

    public interface OnTransactionFeeRequestFinishedListener{
        void onFeeRequestFinished(String priceText);
    }
}
