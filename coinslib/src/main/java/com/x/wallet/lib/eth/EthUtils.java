package com.x.wallet.lib.eth;

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
        if (balance.equals("0")) return "0 ETH";

        return new BigDecimal(balance).divide(new BigDecimal(1000000000000000000d), 6, BigDecimal.ROUND_UP).stripTrailingZeros().toString();
    }
}
