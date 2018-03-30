package com.x.wallet.ui.data;

import android.database.Cursor;

/**
 * Created by wuliang on 18-3-30.
 */

public class TokenItem {
    private int mIdInAll;
    private String mShortname;
    private String mWholename;
    private String mAddress;
    private String mBalance;

    public TokenItem(int idInAll, String shortname, String wholename, String address, String balance) {
        mIdInAll = idInAll;
        mShortname = shortname;
        mWholename = wholename;
        mAddress = address;
        mBalance = balance;
    }

    public int getIdInAll() {
        return mIdInAll;
    }

    public String getShortname() {
        return mShortname;
    }

    public String getWholename() {
        return mWholename;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getBalance() {
        return mBalance;
    }

    public static final int TOKEN_COLUMN_ID_IN_ALL  = 2;
    public static final int TOKEN_COLUMN_ADDRESS    = 3;
    public static final int TOKEN_COLUMN_SHORT_NAME = 4;
    public static final int TOKEN_COLUMN_WHOLE_NAME = 5;
    public static final int TOKEN_COLUMN_BALANCE    = 6;

    public static TokenItem createFromCursor(Cursor cursor) {
        return new TokenItem(
                cursor.getInt(TOKEN_COLUMN_ID_IN_ALL),
                cursor.getString(TOKEN_COLUMN_SHORT_NAME),
                cursor.getString(TOKEN_COLUMN_WHOLE_NAME),
                cursor.getString(TOKEN_COLUMN_ADDRESS),
                cursor.getString(TOKEN_COLUMN_BALANCE));
    }
}
