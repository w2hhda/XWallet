package com.x.wallet.ui.data;

import android.database.Cursor;

import java.io.Serializable;

public class AddressItem implements Serializable{
    private String name;
    private String address;
    private String addressType;

    public AddressItem(String name, String address, String addressType) {
        this.name = name;
        this.address = address;
        this.addressType = addressType;
    }

    public static AddressItem createFromCursor(Cursor cursor){
        return new AddressItem(cursor.getString(COLUMN_NAME)
            , cursor.getString(COLUMN_ADDRESS), cursor.getString(COLUMN_ADDRESS_TYPE));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    static final int COLUMN_ID           = 0;
    static final int COLUMN_ADDRESS      = 1;
    static final int COLUMN_ADDRESS_TYPE = 2;
    static final int COLUMN_NAME         = 3;
}
