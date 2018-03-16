package com.x.wallet.ui.data;

import android.database.Cursor;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountItem {
    private String mAccountName;
    private String mCoinName;
    private long mBalance;

    public AccountItem(String accountName, String coinName, long balance) {
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
        return new AccountItem(cursor.getString(COLUMN_ACCOUNT_NAME),
                cursor.getString(COLUMN_COIN_NAME),
                0);
    }

    static final int COLUMN_ACCOUNT_NAME            = 2;
    static final int COLUMN_COIN_NAME               = 3;
}
