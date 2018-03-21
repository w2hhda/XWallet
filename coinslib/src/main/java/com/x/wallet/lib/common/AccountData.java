package com.x.wallet.lib.common;

/**
 * Created by wuliang on 18-3-14.
 */

public class AccountData {
    private String mAddress;
    private String mAccountName;
    private String mCoinName;
    private int mCoinType;

    private String mEncryptSeed;
    private String mEncryptMnemonic;
    private String mEncryptPrivKey;
    private String mKeyStore;

    public AccountData(String address, String encryptSeed, String encryptMnemonic, String encryptPrivKey, String keyStore) {
        mAddress = address;
        mEncryptSeed = encryptSeed;
        mEncryptMnemonic = encryptMnemonic;
        mEncryptPrivKey = encryptPrivKey;
        mKeyStore = keyStore;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public void setAccountName(String accountName) {
        mAccountName = accountName;
    }

    public String getCoinName() {
        return mCoinName;
    }

    public void setCoinName(String coinName) {
        mCoinName = coinName;
    }

    public int getCoinType() {
        return mCoinType;
    }

    public void setCoinType(int coinType) {
        mCoinType = coinType;
    }

    public String getEncryptSeed() {
        return mEncryptSeed;
    }

    public void setEncryptSeed(String encryptSeed) {
        mEncryptSeed = encryptSeed;
    }

    public String getEncryptMnemonic() {
        return mEncryptMnemonic;
    }

    public void setEncryptMnemonic(String encryptMnemonic) {
        mEncryptMnemonic = encryptMnemonic;
    }

    public String getEncryptPrivKey() {
        return mEncryptPrivKey;
    }

    public void setEncryptPrivKey(String encryptPrivKey) {
        mEncryptPrivKey = encryptPrivKey;
    }

    public String getKeyStore() {
        return mKeyStore;
    }

    public void setKeyStore(String keyStore) {
        mKeyStore = keyStore;
    }

    @Override
    public String toString() {
        return "AccountData{" +
                "mAddress='" + mAddress + '\'' +
                ", mAccountName='" + mAccountName + '\'' +
                ", mCoinName='" + mCoinName + '\'' +
                ", mCoinType=" + mCoinType +
                ", mEncryptSeed='" + mEncryptSeed + '\'' +
                ", mEncryptMnemonic='" + mEncryptMnemonic + '\'' +
                ", mEncryptPrivKey='" + mEncryptPrivKey + '\'' +
                ", mKeyStore='" + mKeyStore + '\'' +
                '}';
    }
}
