package com.x.wallet.ui.data;

import android.database.Cursor;

import java.io.Serializable;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountItem implements Serializable {
    private String mAddress;
    private String mAccountName;
    private String mCoinName;
    private long mBalance;

    public AccountItem(String address, String accountName, String coinName, long balance) {
        mAddress = address;
        mAccountName = accountName;
        mCoinName = coinName;
        mBalance = balance;
    }

    public void setAccountName(String accountName) {
        mAccountName = accountName;
    }

    public void setCoinName(String coinName) {
        mCoinName = coinName;
    }

    public void setBalance(long balance) {
        mBalance = balance;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public String getCoinName() {
        return mCoinName;
    }

    public long getBalance() {
        return mBalance;
    }

    public static AccountItem createFromCursor(Cursor cursor){
        return new AccountItem(cursor.getString(COLUMN_ACCOUNT_ADDRESS),
                cursor.getString(COLUMN_ACCOUNT_NAME),
                cursor.getString(COLUMN_COIN_NAME),
                0);
    }

    static final int COLUMN_ACCOUNT_ADDRESS         = 1;
    static final int COLUMN_ACCOUNT_NAME            = 2;
    static final int COLUMN_COIN_NAME               = 3;
}
