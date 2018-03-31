package com.x.wallet.transaction.token;

import java.math.BigDecimal;

/**
 * Created by wuliang on 18-3-31.
 */

public class TokenUtils {
    public static BigDecimal translateToken(String balance, int decimals){
        BigDecimal bigDecimalBalance = new BigDecimal(balance);
        BigDecimal bigDecimalDecimals = new BigDecimal("1000000000000000000");
        return bigDecimalBalance.divide(bigDecimalDecimals, 6, BigDecimal.ROUND_UP);
    }


    public static double calculateTokenBalance(String balance, int decimals, double rate){
        BigDecimal token = translateToken(balance, decimals);
        return token.doubleValue() * rate;
    }
}
