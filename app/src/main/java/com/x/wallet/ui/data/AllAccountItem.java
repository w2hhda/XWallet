package com.x.wallet.ui.data;

import android.database.Cursor;

import com.x.wallet.db.DbUtils;

/**
 * Created by wuliang on 18-4-2.
 */

public class AllAccountItem {
    private long mId;
    private long mSelfId;
    private String mAddress;
    private String mAccountName;
    private String mCoinName;
    private int mCoinType;
    private String mEncrySeed;
    private String mEncryMnemonic;
    private String mKeyStore;
    private String mBalance;
    private int mDecimals;
    private double mRate;
    private String mContractAddress;
    private int mAllCoinType;

    public AllAccountItem(long id, long selfId, String address, String accountName, String coinName,
                          int coinType, String encrySeed, String encryMnemonic, String keyStore,
                          String balance,
                          int decimals, double rate, String contractAddress, int allCoinType) {
        mId = id;
        mSelfId = selfId;
        mAddress = address;
        mAccountName = accountName;
        mCoinName = coinName;
        mCoinType = coinType;
        mEncrySeed = encrySeed;
        mEncryMnemonic = encryMnemonic;
        mKeyStore = keyStore;
        mBalance = balance;
        mDecimals = decimals;
        mRate = rate;
        mContractAddress = contractAddress;
        mAllCoinType = allCoinType;
    }

    public long getId() {
        return mId;
    }

    public long getSelfId() {
        return mSelfId;
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

    public int getCoinType() {
        return mCoinType;
    }

    public String getEncrySeed() {
        return mEncrySeed;
    }

    public String getEncryMnemonic() {
        return mEncryMnemonic;
    }

    public String getKeyStore() {
        return mKeyStore;
    }

    public String getBalance() {
        return mBalance;
    }

    public int getDecimals() {
        return mDecimals;
    }

    public double getRate() {
        return mRate;
    }

    public String getContractAddress() {
        return mContractAddress;
    }

    public int getAllCoinType() {
        return mAllCoinType;
    }

    public boolean isToken() {
        return mAllCoinType == AllAccountItem.COIN_TYPE_TOKEN;
    }

    @Override
    public String toString() {
        return "AllAccountItem{" +
                "mAddress='" + mAddress + '\'' +
                ", mAccountName='" + mAccountName + '\'' +
                ", mCoinName='" + mCoinName + '\'' +
                ", mCoinType=" + mCoinType +
                '}';
    }

    public static AllAccountItem createFromCursor(Cursor cursor){
        return new AllAccountItem(cursor.getLong(COLUMN_ID),
                cursor.getLong(COLUMN_SELF_ID),
                cursor.getString(COLUMN_ACCOUNT_ADDRESS),
                cursor.getString(COLUMN_ACCOUNT_NAME),
                cursor.getString(COLUMN_COIN_NAME),
                cursor.getInt(COLUMN_COIN_TYPE),
                cursor.getString(COLUMN_COIN_SEED),
                cursor.getString(COLUMN_COIN_MNEMONIC),
                cursor.getString(COLUMN_KEYSTORE),
                cursor.getString(COLUMN_BALANCE),
                cursor.getInt(COLUMN_DECIMALS),
                cursor.getDouble(COLUMN_RATE),
                cursor.getString(COLUMN_CONTRACT_ADDRESS),
                cursor.getInt(COLUMN_ALL_COIN_TYPE)
                );
    }

    public static SerializableAccountItem translateToSerializable(AllAccountItem accountItem) {
        return new SerializableAccountItem(accountItem.getId(),
                accountItem.getAddress(),
                accountItem.getAccountName(),
                accountItem.getCoinName(),
                accountItem.getCoinType(),
                accountItem.getEncrySeed(),
                accountItem.getEncryMnemonic(),
                accountItem.getKeyStore(),
                null,
                accountItem.getBalance(),
                false);
    }

    public static final String ALL_COIN_TYPE = "all_coin_type";
    public static final int COIN_TYPE_MAIN = 1;
    public static final int COIN_TYPE_TOKEN = 2;

    public static final String[] PROJECTION = {
            DbUtils.DbColumns._ID,
            "self_id",
            DbUtils.DbColumns.ADDRESS,
            DbUtils.DbColumns.NAME,
            DbUtils.DbColumns.COIN_NAME,
            DbUtils.DbColumns.COIN_TYPE,
            DbUtils.DbColumns.ENCRYPT_SEED,
            DbUtils.DbColumns.ENCRYPT_MNEMONIC,
            DbUtils.DbColumns.KEYSTORE,
            DbUtils.DbColumns.BALANCE,
            DbUtils.TokenTableColumns.DECIMALS,
            DbUtils.TokenTableColumns.RATE,
            DbUtils.TokenTableColumns.CONTRACT_ADDRESS,
            ALL_COIN_TYPE
    };

    static final int COLUMN_ID                      = 0;
    static final int COLUMN_SELF_ID                 = 1;
    static final int COLUMN_ACCOUNT_ADDRESS         = 2;
    static final int COLUMN_ACCOUNT_NAME            = 3;
    static final int COLUMN_COIN_NAME               = 4;
    static final int COLUMN_COIN_TYPE               = 5;
    static final int COLUMN_COIN_SEED               = 6;
    static final int COLUMN_COIN_MNEMONIC           = 7;
    static final int COLUMN_KEYSTORE                = 8;
    static final int COLUMN_BALANCE                 = 9;
    static final int COLUMN_DECIMALS                = 10;
    static final int COLUMN_RATE                    = 11;
    static final int COLUMN_CONTRACT_ADDRESS        = 12;
    static final int COLUMN_ALL_COIN_TYPE           = 13;
}
