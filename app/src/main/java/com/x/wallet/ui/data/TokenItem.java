package com.x.wallet.ui.data;

import android.database.Cursor;

/**
 * Created by wuliang on 18-3-30.
 */

public class TokenItem {
    private String mName;
    private String mSymbol;
    private int mDecimals;
    private String mBalance;
    private double mRate;
    private String mContractAddress;

    public TokenItem(String name, String symbol, int decimals, String balance, double rate, String contractAddress) {
        mName = name;
        mSymbol = symbol;
        mDecimals = decimals;
        mBalance = balance;
        mRate = rate;
        mContractAddress = contractAddress;
    }

    public String getName() {
        return mName;
    }

    public String getSymbol() {
        return mSymbol;
    }

    public int getDecimals() {
        return mDecimals;
    }

    public String getBalance() {
        return mBalance;
    }

    public double getRate() {
        return mRate;
    }

    public String getContractAddress() {
        return mContractAddress;
    }

    public static final int TOKEN_COLUMN_NAME    = 3;
    public static final int TOKEN_COLUMN_SYMBOL = 4;
    public static final int TOKEN_COLUMN_DECIMALS = 5;
    public static final int TOKEN_COLUMN_CONTRACT_ADDRESS = 6;
    public static final int TOKEN_COLUMN_BALANCE    = 7;
    public static final int TOKEN_COLUMN_RATE    = 8;

    public static TokenItem createFromCursor(Cursor cursor) {
        return new TokenItem(
                cursor.getString(TOKEN_COLUMN_NAME),
                cursor.getString(TOKEN_COLUMN_SYMBOL),
                cursor.getInt(TOKEN_COLUMN_DECIMALS),
                cursor.getString(TOKEN_COLUMN_BALANCE),
                cursor.getDouble(TOKEN_COLUMN_RATE),
                cursor.getString(TOKEN_COLUMN_CONTRACT_ADDRESS)
        );
    }
}
