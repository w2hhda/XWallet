package com.x.wallet.transaction.token;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by wuliang on 18-3-31.
 */

public class TokenUtils {
    public static final Uri QUERY_TOKEN_BALANCE_URI = Uri.parse("content://com.x.wallet/token/");
    public static final int DECIMAL_COUNT = 2;

    public static BigDecimal translateTokenInWholeUnit(String rawBalance, int decimals) {
        if (TextUtils.isEmpty(rawBalance) || rawBalance.equals("0")) return BigDecimal.ZERO;

        BigDecimal bigDecimalBalance = new BigDecimal(rawBalance);
        return bigDecimalBalance.divide(translateDecimalsIntoBigDecimal(decimals), DECIMAL_COUNT, BigDecimal.ROUND_UP);
    }

    public static String getTokenConversionText(Context context, double translateBalance, double rate) {
        return context.getString(R.string.item_balance, calculateTokenBalance2(translateBalance, rate));
    }

    public static String getTokenConversionText(Context context, BigDecimal translateBalance, double rate) {
        if(translateBalance.compareTo(BigDecimal.ZERO) == 0){
            return context.getString(R.string.blank_balance);
        }
        return context.getString(R.string.item_balance, calculateTokenBalance(translateBalance, rate));
    }

    public static BigDecimal translateDecimalsIntoBigDecimal(int decimals) {
        BigDecimal bd = new BigDecimal("10");
        return bd.pow(decimals);
    }

    public static String formatDouble(double value) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(value);
    }

    public static String getStrFromBigDecimal(BigDecimal bigDecimal) {
        return bigDecimal.stripTrailingZeros().toPlainString();
    }

    public static double calculateTokenBalance(double balance, double rate) {
        return balance * rate;
    }

    public static String calculateTokenBalance(BigDecimal translateBalance, double rate) {
        return formatDouble(translateBalance.doubleValue() * rate * UsdToCnyHelper.mUsdToCny);
    }

    public static String calculateTokenBalance2(double translateBalance, double rate) {
        Log.i(AppUtils.APP_TAG, "TokenUtils BalanceConversionUtils.mUsdToCny = " + UsdToCnyHelper.mUsdToCny);
        return formatDouble(translateBalance * rate * UsdToCnyHelper.mUsdToCny);
    }
}
