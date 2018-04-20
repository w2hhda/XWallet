package com.x.wallet.ui.data;

import android.database.Cursor;

import com.x.wallet.AppUtils;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountItem {
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

    public AccountItem(long id, String address, String accountName, String coinName,
                       int coinType, String encrySeed, String encryMnemonic, String keyStore,
                       String privKey, String balance, int hasBackup) {
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
        mHasBackup = hasBackup == AppUtils.HAS_BACKUP;
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

    public String getEncryMnemonic() {
        return mEncryMnemonic;
    }

    public String getEncrySeed() {
        return mEncrySeed;
    }

    public String getKeyStore() {
        return mKeyStore;
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

    public boolean isHasBackup() {
        return mHasBackup;
    }

    public static SerializableAccountItem translateToSerializable(AccountItem accountItem) {
        return new SerializableAccountItem(accountItem.getId(),
                                        accountItem.getAddress(),
                                        accountItem.getAccountName(),
                                        accountItem.getCoinName(),
                                        accountItem.getCoinType(),
                                        accountItem.getEncrySeed(),
                                        accountItem.getEncryMnemonic(),
                                        accountItem.getKeyStore(),
                                        accountItem.getPrivKey(),
                                        accountItem.getBalance(),
                                        accountItem.mHasBackup);
    }
    public static AccountItem createFromCursor(Cursor cursor) {
        return new AccountItem(cursor.getLong(COLUMN_ID),
                cursor.getString(COLUMN_ACCOUNT_ADDRESS),
                cursor.getString(COLUMN_ACCOUNT_NAME),
                cursor.getString(COLUMN_COIN_NAME),
                cursor.getInt(COLUMN_COIN_TYPE),
                cursor.getString(COLUMN_COIN_SEED),
                cursor.getString(COLUMN_COIN_MNEMONIC),
                cursor.getString(COLUMN_KEYSTORE),
                cursor.getString(COLUMN_RPIV_KEY),
                cursor.getString(COLUMN_BALANCE),
                cursor.getInt(COLUMN_HAS_BACKUP));
    }

    @Override
    public String toString() {
        return "AccountItem{" +
                "mAddress='" + mAddress + '\'' +
                ", mAccountName='" + mAccountName + '\'' +
                ", mCoinName='" + mCoinName + '\'' +
                ", mCoinType=" + mCoinType +
                '}';
    }

    static final int COLUMN_ID = 0;
    static final int COLUMN_ACCOUNT_ADDRESS = 1;
    static final int COLUMN_ACCOUNT_NAME = 2;
    static final int COLUMN_COIN_NAME = 3;
    static final int COLUMN_COIN_TYPE = 4;
    static final int COLUMN_COIN_SEED = 5;
    static final int COLUMN_COIN_MNEMONIC = 6;
    static final int COLUMN_RPIV_KEY = 7;
    static final int COLUMN_KEYSTORE = 8;
    static final int COLUMN_BALANCE = 9;
    static final int COLUMN_HAS_BACKUP = 10;
}
