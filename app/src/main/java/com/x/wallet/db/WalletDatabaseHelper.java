package com.x.wallet.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.btc.BtcDbHelper;

/**
 * Created by wuliang on 18-3-14.
 */

public class WalletDatabaseHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "xwallet.db";
    static final int DATABASE_VERSION = 1;
    private final Context mContext;

    private static WalletDatabaseHelper sInstance = null;

    private WalletDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;
    }

    public static synchronized WalletDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WalletDatabaseHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private void createTables(SQLiteDatabase db){
        Log.i(AppUtils.APP_TAG, "WalletDatabaseHelper createTables create");
        db.execSQL("CREATE TABLE " + XWalletProvider.TABLE_ACCOUNT + " (" +
                DbUtils.DbColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DbUtils.DbColumns.ADDRESS + " TEXT," +
                DbUtils.DbColumns.NAME + " TEXT," +
                DbUtils.DbColumns.COIN_NAME + " TEXT," +
                DbUtils.DbColumns.COIN_TYPE + " INTEGER," +
                DbUtils.DbColumns.ENCRYPT_SEED + " TEXT," +
                DbUtils.DbColumns.ENCRYPT_MNEMONIC + " TEXT," +
                DbUtils.DbColumns.ENCRYPT_PRIV_KEY + " TEXT," +
                DbUtils.DbColumns.KEYSTORE + " TEXT," +
                DbUtils.DbColumns.BALANCE + " TEXT DEFAULT 0," +
                DbUtils.DbColumns.HAS_TOKEN + " INTEGER DEFAULT 0," +
                DbUtils.DbColumns.PUB_KEY + " TEXT," +
                DbUtils.DbColumns.IS_SYNCED + " INTEGER DEFAULT 1);");

        db.execSQL("CREATE TABLE " + XWalletProvider.TABLE_TOKEN + " (" +
                DbUtils.TokenTableColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DbUtils.TokenTableColumns.ACCOUNT_ID + " INTEGER," +
                DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " TEXT," +
                DbUtils.TokenTableColumns.ID_IN_ALL + " INTEGER," +
                DbUtils.TokenTableColumns.NAME + " TEXT," +
                DbUtils.TokenTableColumns.SYMBOL + " TEXT," +
                DbUtils.TokenTableColumns.DECIMALS + " INTEGER," +
                DbUtils.TokenTableColumns.CONTRACT_ADDRESS + " TEXT," +
                DbUtils.TokenTableColumns.BALANCE + " TEXT DEFAULT 0," +
                DbUtils.TokenTableColumns.RATE + " TEXT DEFAULT 0);");

        db.execSQL("CREATE TABLE " + XWalletProvider.TABLE_TRANSACTION + " (" +
                DbUtils.TxTableColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DbUtils.TxTableColumns.TX_HASH + " TEXT," +
                DbUtils.TxTableColumns.TIME_STAMP + " TEXT," +
                DbUtils.TxTableColumns.NONCE + " TEXT," +
                DbUtils.TxTableColumns.FROM_ADDRESS + " TEXT," +
                DbUtils.TxTableColumns.TO_ADDRESS + " TEXT," +
                DbUtils.TxTableColumns.VALUE + " TEXT," +
                DbUtils.TxTableColumns.GAS_LIMIT + " TEXT," +
                DbUtils.TxTableColumns.GAS_PRICE + " TEXT," +
                DbUtils.TxTableColumns.IS_ERROR + " TEXT," +
                DbUtils.TxTableColumns.TX_RECEIPT_STATUS + " TEXT," +
                DbUtils.TxTableColumns.INPUT_DATA + " TEXT," +
                DbUtils.TxTableColumns.GAS_USED + " TEXT," +
                DbUtils.TxTableColumns.CONTRACT_ADDRESS + " TEXT," +
                DbUtils.TxTableColumns.TOKEN_SYMBOL + " TEXT," +
                DbUtils.TxTableColumns.TOKEN_NAME + " TEXT," +
                DbUtils.TxTableColumns.TOKEN_DECIMALS + " INTEGER DEFAULT 0," +
                DbUtils.TxTableColumns.BLOCK_NUMBER + " TEXT DEFAULT 0);");

        BtcDbHelper.createBitCoinTable(db);
    }
}
