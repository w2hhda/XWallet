package com.x.wallet.transaction;

import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.util.ExchangeCalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by wuliang on 18-4-17.
 */

public class EthTransactionFeeHelper {
    private BigInteger defaultGasLimit = new BigInteger("91000");
    private BigDecimal defaultPrice = new BigDecimal(0);
    private OnPriceChangedListener mOnPriceChangedListener;

    public EthTransactionFeeHelper(OnPriceChangedListener onPriceChangedListener) {
        mOnPriceChangedListener = onPriceChangedListener;
    }

    public void getGasPrice() {
        try {
            EtherscanAPI.getInstance().getGasPrice(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(AppUtils.APP_TAG, "EthTransactionFeeHelper getGasPrice IOException", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        final String result = object.getString("result").substring(2);
                        final BigInteger price = new BigInteger(result, 16);
                        Log.i(AppUtils.APP_TAG, "EthTransactionFeeHelper getGasPrice price = " + price);
                        BigDecimal nowPrice = ExchangeCalUtil.getInstance().weiToEther(price.multiply(defaultGasLimit));
                        setDefaultPrice(nowPrice);
                        if (mOnPriceChangedListener != null) {
                            mOnPriceChangedListener.onPriceChanged(nowPrice + "");
                        }
                    } catch (JSONException e) {
                        Log.e(AppUtils.APP_TAG, "EthTransactionFeeHelper getGasPrice JSONException", e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(AppUtils.APP_TAG, "EthTransactionFeeHelper getGasPrice Exception", e);
        }
    }

    public void updateGasPrice(final int progress) {
        final BigDecimal half = new BigDecimal(0.5);
        final BigDecimal dec = new BigDecimal(10);
        final BigDecimal nowPrice = getDefaultPrice();
        BigDecimal now;
        if (progress == 0) {
            now = nowPrice.multiply(half);
        } else if (progress == 100) {
            now = nowPrice.multiply(dec);
        } else {
            now = nowPrice;
        }
        if (mOnPriceChangedListener != null) {
            mOnPriceChangedListener.onPriceChanged(now.toString());
        }
    }

    public BigDecimal getNowPrice(String nowPrice){
        BigDecimal ethTowei = new BigDecimal("1000000000000000000");
        return new BigDecimal(nowPrice).multiply(ethTowei).divide(new BigDecimal(defaultGasLimit));
    }

    private void setDefaultPrice(BigDecimal number) {
        this.defaultPrice = number;
    }

    private BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public BigInteger getDefaultGasLimit() {
        return defaultGasLimit;
    }

    public interface OnPriceChangedListener {
        public void onPriceChanged(String newPriceText);
    }
}
