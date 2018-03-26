package com.x.wallet.transaction.balance;

import com.x.wallet.lib.eth.EthUtils;

import java.math.BigDecimal;

/**
 * Created by wuliang on 18-3-26.
 */

public class BalanceConversionUtils {
    public static BigDecimal mAllBalance = new BigDecimal(0);
    public static final String ZERO = "0";
    public static double mEthToUsd = 0;
    public static double mUsdToCny = 0;

    private static RateUpdateListener mRateUpdateListener;

    public static void setRateUpdateListener(RateUpdateListener rateUpdateListener) {
        mRateUpdateListener = rateUpdateListener;
    }

    public static void responseToListener() {
        if(mRateUpdateListener != null){
            mRateUpdateListener.onRateUpdate();
        }
    }

    public static String calculateBalanceText() {
        if(mEthToUsd == 0 || mUsdToCny == 0){
            return ZERO;
        }

        BigDecimal result = EthUtils.translateWeiToEth(mAllBalance).multiply(new BigDecimal(mEthToUsd * mUsdToCny)).setScale(6, BigDecimal.ROUND_UP);
        return result.stripTrailingZeros().toString();
    }

    public interface RateUpdateListener{
        void onRateUpdate();
    }
}
