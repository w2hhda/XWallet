package com.x.wallet.transaction.balance;

import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.lib.eth.EthUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by wuliang on 18-3-26.
 */

public class BalanceConversionUtils {
    public static BigDecimal mAllBalance = new BigDecimal(0);
    public static final String ZERO = "0";
    public static double mEthToUsd = 0;
    public static double mUsdToCny = 0;

    private static RateUpdateListener mRateUpdateListener;
    private static Set<RateUpdateListener> mListenerSet = new HashSet<>();

    public static void setRateUpdateListener(RateUpdateListener rateUpdateListener) {
        mRateUpdateListener = rateUpdateListener;
    }

    public static void responseToListener() {
        if(mRateUpdateListener != null){
            mRateUpdateListener.onRateUpdate();
        }
    }

    public static void registerListener(RateUpdateListener listener){
        mListenerSet.add(listener);
    }

    public static void unRegisterListener(RateUpdateListener listener){
        mListenerSet.remove(listener);
    }

    public static void handleListener(){
        for(RateUpdateListener listener : mListenerSet){
            listener.onRateUpdate();
        }
    }

    public static String calculateAllBalanceText() {
        if(mEthToUsd == 0 || mUsdToCny == 0){
            return ZERO;
        }

        BigDecimal result = EthUtils.translateWeiToEth(mAllBalance).multiply(new BigDecimal(mEthToUsd * mUsdToCny)).setScale(6, BigDecimal.ROUND_UP);
        return result.stripTrailingZeros().toString();
    }

    public static String calculateBalanceText(String balance) {
        if(mEthToUsd == 0 || mUsdToCny == 0){
            return ZERO;
        }

        BigDecimal result = EthUtils.translateWeiToEth(balance).multiply(new BigDecimal(mEthToUsd * mUsdToCny)).setScale(6, BigDecimal.ROUND_UP);
        return result.stripTrailingZeros().toString();
    }

    public static void clearListener() {
        setRateUpdateListener(null);
        Log.i(AppUtils.APP_TAG, "BalanceConversionUtils clearListener mListenerSet.size = " + mListenerSet.size());
        mListenerSet.clear();
    }

    public interface RateUpdateListener{
        void onRateUpdate();
    }
}
