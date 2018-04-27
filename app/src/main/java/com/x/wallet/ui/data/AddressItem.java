package com.x.wallet.ui.data;

import android.database.Cursor;

import java.io.Serializable;

public class AddressItem implements Serializable {
    private long mId;
    private String mName;
    private String mAddress;
    private String mAddressType;

    public AddressItem(long id, String address, String addressType, String name) {
        mId = id;
        mAddress = address;
        mAddressType = addressType;
        mName = name;
    }

    public static AddressItem createFromCursor(Cursor cursor) {
        return new AddressItem(cursor.getLong(COLUMN_ID),
                cursor.getString(COLUMN_ADDRESS),
                cursor.getString(COLUMN_ADDRESS_TYPE),
                cursor.getString(COLUMN_NAME));
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public String getAddressType() {
        return mAddressType;
    }

    public long getId() {
        return mId;
    }

    static final int COLUMN_ID = 0;
    static final int COLUMN_ADDRESS = 1;
    static final int COLUMN_ADDRESS_TYPE = 2;
    static final int COLUMN_NAME = 3;

    @Override
    public String toString() {

        return mName + "'s " + mAddressType + " Address : \n" + mAddress;
    }
}
