package com.x.wallet.lib.eth.data;

/**
 * Created by wuliang on 18-3-26.
 */
//{"base":"USD","date":"2018-03-23","rates":{"CNY":6.3158}}
public class UsdCnyBean {
    private String base;
    private String date;
    private Rate rates;

    public class Rate{
        private double CNY;

        public double getCNY() {
            return CNY;
        }
    }

    public Rate getRates() {
        return rates;
    }
}
