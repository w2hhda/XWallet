package com.x.wallet.lib.btc;

/**
 * Created by wuliang on 18-3-14.
 */

public class AccountData {
    private String mAddress;
    private String mAccountName;
    private String mCoinName;
    private String mEncryptSeed;
    private String mEncryptMnemonic;

    public AccountData(String address, String encryptSeed, String encryptMnemonic) {
        mAddress = address;
        mEncryptSeed = encryptSeed;
        mEncryptMnemonic = encryptMnemonic;
    }

    public void setAccountName(String accountName) {
        mAccountName = accountName;
    }

    public void setCoinName(String coinName) {
        mCoinName = coinName;
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

    public String getEncryptSeed() {
        return mEncryptSeed;
    }

    public String getEncryptMnemonic() {
        return mEncryptMnemonic;
    }

    @Override
    public String toString() {
        return "AccountData{" +
                "mAddress='" + mAddress + '\'' +
                ", mAccountName='" + mAccountName + '\'' +
                ", mCoinName='" + mCoinName + '\'' +
                ", mEncryptSeed='" + mEncryptSeed + '\'' +
                ", mEncryptMnemonic='" + mEncryptMnemonic + '\'' +
                '}';
    }
}
