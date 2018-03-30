package com.x.wallet.ui.data;

/**
 * Created by wuliang on 18-3-30.
 */

public class RawAccountItem {
    private String mCoinName;
    private int mCoinType;
    private String mBalance;

    public RawAccountItem(String coinName, int coinType, String balance) {
        mCoinName = coinName;
        mCoinType = coinType;
        mBalance = balance;
    }

    public String getCoinName() {
        return mCoinName;
    }

    public void setCoinName(String coinName) {
        mCoinName = coinName;
    }

    public int getCoinType() {
        return mCoinType;
    }

    public void setCoinType(int coinType) {
        mCoinType = coinType;
    }

    public String getBalance() {
        return mBalance;
    }

    public void setBalance(String balance) {
        mBalance = balance;
    }
}
