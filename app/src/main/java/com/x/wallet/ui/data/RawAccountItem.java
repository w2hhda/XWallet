package com.x.wallet.ui.data;

/**
 * Created by wuliang on 18-3-30.
 */

public class RawAccountItem {
    private String mCoinName;
    private int mCoinType;
    private String mBalance;
    private int mDecimals;
    private double mRate;

    public RawAccountItem(String coinName, int coinType, String balance, int decimals, double rate) {
        mCoinName = coinName;
        mCoinType = coinType;
        mBalance = balance;
        mDecimals = decimals;
        mRate = rate;
    }

    public String getCoinName() {
        return mCoinName;
    }

    public int getCoinType() {
        return mCoinType;
    }

    public String getBalance() {
        return mBalance;
    }

    public int getDecimals() {
        return mDecimals;
    }

    public double getRate() {
        return mRate;
    }
}
