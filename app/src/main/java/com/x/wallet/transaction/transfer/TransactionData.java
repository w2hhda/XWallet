package com.x.wallet.transaction.transfer;

import java.math.BigInteger;

/**
 * Created by wuliang on 18-4-28.
 */

public class TransactionData {
    private String mPassword;
    private String mAddress;

    private String mFromAddress;
    private String mToAddress;
    private String mGasPrice;
    private BigInteger mGasLimit;
    private String mAmount;
    private String mExtraData;

    public TransactionData(String password, String address,
                           String fromAddress, String toAddress,
                           String gasPrice, BigInteger gasLimit,
                           String amount, String extraData) {
        mPassword = password;
        mAddress = address;
        mFromAddress = fromAddress;
        mToAddress = toAddress;
        mGasPrice = gasPrice;
        mGasLimit = gasLimit;
        mAmount = amount;
        mExtraData = extraData;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getFromAddress() {
        return mFromAddress;
    }

    public String getToAddress() {
        return mToAddress;
    }

    public String getGasPrice() {
        return mGasPrice;
    }

    public BigInteger getGasLimit() {
        return mGasLimit;
    }

    public String getAmount() {
        return mAmount;
    }

    public String getExtraData() {
        return mExtraData;
    }
}
