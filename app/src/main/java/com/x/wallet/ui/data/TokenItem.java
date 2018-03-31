package com.x.wallet.ui.data;

import android.database.Cursor;

/**
 * Created by wuliang on 18-3-30.
 */

public class TokenItem {
    private int mIdInAll;
    private String mName;
    private String mSymbol;
    private int mDecimals;
    private String mBalance;
    private double mRate;

    public TokenItem(int idInAll, String name, String symbol, int decimals, String balance, double rate) {
        mIdInAll = idInAll;
        mName = name;
        mSymbol = symbol;
        mDecimals = decimals;
        mBalance = balance;
        mRate = rate;
    }

    public int getIdInAll() {
        return mIdInAll;
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

    public static final int TOKEN_COLUMN_ID_IN_ALL  = 3;
    public static final int TOKEN_COLUMN_NAME    = 4;
    public static final int TOKEN_COLUMN_SYMBOL = 5;
    public static final int TOKEN_COLUMN_DECIMALS = 6;
    public static final int TOKEN_COLUMN_BALANCE    = 8;
    public static final int TOKEN_COLUMN_RATE    = 9;

    public static TokenItem createFromCursor(Cursor cursor) {
        return new TokenItem(
                cursor.getInt(TOKEN_COLUMN_ID_IN_ALL),
                cursor.getString(TOKEN_COLUMN_NAME),
                cursor.getString(TOKEN_COLUMN_SYMBOL),
                cursor.getInt(TOKEN_COLUMN_DECIMALS),
                cursor.getString(TOKEN_COLUMN_BALANCE),
                cursor.getDouble(TOKEN_COLUMN_RATE));
    }
}
