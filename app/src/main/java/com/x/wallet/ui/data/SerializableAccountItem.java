package com.x.wallet.ui.data;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by wuliang on 18-3-16.
 */

public class SerializableAccountItem implements Serializable {
    private long mId;
    private String mAddress;
    private String mAccountName;
    private String mCoinName;
    private int mCoinType;
    private String mEncrySeed;
    private String mEncryMnemonic;
    private String mKeyStore;
    private String mPrivKey;
    private String mBalance;
    private boolean mHasBackup;

    public SerializableAccountItem(long id, String address, String accountName, String coinName,
                       int coinType, String encrySeed, String encryMnemonic, String keyStore, String privKey,
                       String balance, boolean hasBackup) {
        mId = id;
        mAddress = address;
        mAccountName = accountName;
        mCoinName = coinName;
        mCoinType = coinType;
        mEncrySeed = encrySeed;
        mEncryMnemonic = encryMnemonic;
        mKeyStore = keyStore;
        mPrivKey = privKey;
        mBalance = balance;
        mHasBackup = hasBackup;
    }

    public long getId() {
        return mId;
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

    public String getCoinName() {
        return mCoinName;
    }

    public int getCoinType() {
        return mCoinType;
    }

    public void setCoinType(int coinType) {
        mCoinType = coinType;
    }

    public void setEncrySeed(String encrySeed) {
        mEncrySeed = encrySeed;
    }

    public String getEncrySeed() {
        return mEncrySeed;
    }

    public String getEncryMnemonic() {
        return mEncryMnemonic;
    }

    public void setEncryMnemonic(String encryMnemonic) {
        mEncryMnemonic = encryMnemonic;
    }

    public String getKeyStore() {
        return mKeyStore;
    }

    public void setKeyStore(String keyStore) {
        mKeyStore = keyStore;
    }

    public void setPrivKey(String privKey) {
        mPrivKey = privKey;
    }

    public String getPrivKey() {
        return mPrivKey;
    }

    public String getBalance() {
        return mBalance;
    }

    public void setBalance(String balance) {
        mBalance = balance;
    }

    public boolean hasMnemonic(){
        return !TextUtils.isEmpty(mEncryMnemonic);
    }

    public boolean hasKey(){
        return !TextUtils.isEmpty(mPrivKey);
    }

    public boolean hasKeyStore(){
        return !TextUtils.isEmpty(mKeyStore);
    }

    public void setHasBackup(boolean hasBackup) {
        mHasBackup = hasBackup;
    }

    public boolean isHasBackup() {
        return mHasBackup;
    }

    @Override
    public String toString() {
        return "SerializableAccountItem{" +
                "mAddress='" + mAddress + '\'' +
                ", mAccountName='" + mAccountName + '\'' +
                ", mCoinName='" + mCoinName + '\'' +
                ", mCoinType=" + mCoinType +
                '}';
    }
}
