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
    private String mEncryMnemonic;
    private String mEncrySeed;
    private long mBalance;

    public AccountItem(String address, String accountName, String coinName,
                       String encrySeed, String encryMnemonic,
                       long balance) {
        mAddress = address;
        mAccountName = accountName;
        mCoinName = coinName;
        mEncrySeed = encrySeed;
        mEncryMnemonic = encryMnemonic;
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

    public void setEncrySeed(String encrySeed) {
        mEncrySeed = encrySeed;
    }

    public void setEncryMnemonic(String encryMnemonic) {
        mEncryMnemonic = encryMnemonic;
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

    public String getEncrySeed() {
        return mEncrySeed;
    }

    public String getEncryMnemonic() {
        return mEncryMnemonic;
    }

    public static AccountItem createFromCursor(Cursor cursor){
        return new AccountItem(cursor.getString(COLUMN_ACCOUNT_ADDRESS),
                cursor.getString(COLUMN_ACCOUNT_NAME),
                cursor.getString(COLUMN_COIN_NAME),
                cursor.getString(COLUMN_COIN_SEED),
                cursor.getString(COLUMN_COIN_MNEMONIC),
                0);
    }

    static final int COLUMN_ACCOUNT_ADDRESS         = 1;
    static final int COLUMN_ACCOUNT_NAME            = 2;
    static final int COLUMN_COIN_NAME               = 3;
    static final int COLUMN_COIN_SEED               = 4;
    static final int COLUMN_COIN_MNEMONIC           = 5;
}
