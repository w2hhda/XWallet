package com.x.wallet.transaction.token;

import android.net.Uri;
import android.text.TextUtils;

import com.x.wallet.transaction.balance.BalanceConversionUtils;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;

import java.math.BigDecimal;

/**
 * Created by wuliang on 18-3-31.
 */

public class TokenUtils {
    public static final String ZERO = "0";
    private static BalanceConversionUtils.RateUpdateListener mRateUpdateListener;

    public static final Uri QUERY_TOKEN_BALANCE_URI = Uri.parse("content://com.x.wallet/token/");
    public static final int DECIMAL_COUNT = 2;
    public static final int BALANCE_DECIMAL_COUNT = 8;
    public static final int ETH_DECIMALS = 18;

    public static void setRateUpdateListener(BalanceConversionUtils.RateUpdateListener rateUpdateListener) {
        mRateUpdateListener = rateUpdateListener;
    }

    public static void responseToListener() {
        if(mRateUpdateListener != null){
            mRateUpdateListener.onRateUpdate();
        }
    }

    public static BigDecimal calculateTokenBalance(BigDecimal balance) {
        return balance.multiply(new BigDecimal(UsdToCnyHelper.mUsdToCny));
    }

    public static BigDecimal calculateTokenBalance(String rawBalance, int decimals, double rate){
        if (TextUtils.isEmpty(rawBalance) || rawBalance.equals(ZERO)) return BigDecimal.ZERO;

        BigDecimal translateBalance = translate(rawBalance, decimals);
        return translateBalance.multiply(new BigDecimal(rate));
    }

    public static BigDecimal calculate(BigDecimal rawBalance, int decimals){
        if(rawBalance.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return calculate(translate(rawBalance, decimals));
    }

    private static BigDecimal calculate(BigDecimal translateBalance){
        return translateBalance.multiply(new BigDecimal(BalanceConversionUtils.mEthToUsd)).multiply(new BigDecimal(UsdToCnyHelper.mUsdToCny));
    }

    public static String getTokenConversionText(String rawBalance, int decimals, double rate) {
        if (TextUtils.isEmpty(rawBalance) || rawBalance.equals(ZERO)) return ZERO;

        BigDecimal tokenBalance = translate(rawBalance, decimals);
        return TokenUtils.formatConversion(calculateTokenBalance(tokenBalance.multiply(new BigDecimal(rate))));
    }

    public static String getBalanceConversionText(String rawBalance, int decimals){
        if (TextUtils.isEmpty(rawBalance) || rawBalance.equals(ZERO)) return ZERO;

        if(BalanceConversionUtils.mEthToUsd == 0 || UsdToCnyHelper.mUsdToCny == 0 ){
            return ZERO;
        }

        BigDecimal ethBalance = translate(rawBalance, decimals);
        return TokenUtils.formatConversion(calculate(ethBalance));
    }

    public static String getBalanceText(String rawBalance, int decimals){
        if (TextUtils.isEmpty(rawBalance) || rawBalance.equals(ZERO)) return ZERO;

        return TokenUtils.format(translate(rawBalance, decimals));
    }

    public static String getBalanceText(long rawBalance, int decimals){
        if (rawBalance == 0) return ZERO;

        return TokenUtils.format(translate(Long.toString(rawBalance), decimals));
    }

    private static BigDecimal translate(String rawBalance, int decimals){
        BigDecimal balance = new BigDecimal(rawBalance);
        return balance.divide(BigDecimal.TEN.pow(decimals));
    }

    public static BigDecimal translate(long rawBalance, int decimals){
        if(rawBalance == 0) return BigDecimal.ZERO;
        BigDecimal balance = new BigDecimal(rawBalance);
        return translate(balance, decimals);
    }

    public static BigDecimal translate(BigDecimal rawBalance, int decimals){
        return rawBalance.divide(BigDecimal.TEN.pow(decimals));
    }

    public static String format(BigDecimal value) {
        return getStrFromBigDecimal(value.setScale(BALANCE_DECIMAL_COUNT, BigDecimal.ROUND_UP));
    }

    public static String formatConversion(BigDecimal value) {
        return value.setScale(DECIMAL_COUNT, BigDecimal.ROUND_UP).toPlainString();
    }

    public static String getStrFromBigDecimal(BigDecimal bigDecimal) {
        return bigDecimal.stripTrailingZeros().toPlainString();
    }

    public static BigDecimal translateToRaw(String translateBalance, int decimals){
        if (TextUtils.isEmpty(translateBalance) || translateBalance.equals(ZERO)) return BigDecimal.ZERO;
        BigDecimal balance = new BigDecimal(translateBalance);
        return balance.multiply(BigDecimal.TEN.pow(decimals));
    }
}
