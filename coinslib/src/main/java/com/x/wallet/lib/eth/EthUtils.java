package com.x.wallet.lib.eth;

import android.text.TextUtils;

import java.math.BigDecimal;

/**
 * Created by wuliang on 18-3-26.
 */

public class EthUtils {
    public static String addPrefix(String address){
        if(address.startsWith("0x")){
            return address;
        } else {
            return "0x" + address;
        }
    }

    public static String removePrefix(String address){
        if(address.startsWith("0x")){
            return address.substring(2, address.length());
        } else {
            return address;
        }
    }

    public static String getBalanceText(String balance) {
        if (TextUtils.isEmpty(balance) || balance.equals("0")) return "0";

        return translateWeiToEth(new BigDecimal(balance)).toString();
    }

    public static BigDecimal translateWeiToEth(BigDecimal weiValue){
        if(weiValue.compareTo(BigDecimal.ZERO) == 0){
            return weiValue;
        }
        return weiValue.divide(new BigDecimal(1000000000000000000d), 6, BigDecimal.ROUND_UP).stripTrailingZeros();
    }

    public static BigDecimal translateWeiToEth(String balance){
        if (TextUtils.isEmpty(balance) || balance.equals("0")) return BigDecimal.ZERO;

        return translateWeiToEth(new BigDecimal(balance));
    }
}
