package com.x.wallet.ui.data;

import java.io.Serializable;

/**
 * Created by wuliang on 18-3-30.
 */

public class RawAccountItem implements Serializable{
    private String mCoinName;
    private int mCoinType;
    private String mBalance;
    private int mDecimals;
    private double mRate;
    private String mContractAddress;

    public RawAccountItem(String coinName, int coinType, String balance, int decimals, double rate, String contractAddress) {
        mCoinName = coinName;
        mCoinType = coinType;
        mBalance = balance;
        mDecimals = decimals;
        mRate = rate;
        mContractAddress = contractAddress;
    }

    @Override
    public String toString() {
        return "RawAccountItem{" +
                "mCoinName='" + mCoinName + '\'' +
                ", mCoinType=" + mCoinType +
                ", mBalance='" + mBalance + '\'' +
                ", mDecimals=" + mDecimals +
                ", mRate=" + mRate +
                ", mContractAddress='" + mContractAddress + '\'' +
                '}';
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

    public String getContractAddress() {
        return mContractAddress;
    }
}
