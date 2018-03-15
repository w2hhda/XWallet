package com.x.wallet.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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

    static synchronized WalletDatabaseHelper getInstance(Context context) {
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
        Log.i("test", "WalletDatabaseHelper createTables create");
        db.execSQL("CREATE TABLE " + XWalletProvider.TABLE_ACCOUNT + " (" +
                DbUtils.DbColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DbUtils.DbColumns.ADDRESS + " TEXT," +
                DbUtils.DbColumns.NAME + " TEXT," +
                DbUtils.DbColumns.COIN_NAME + " TEXT," +
                DbUtils.DbColumns.ENCRYPT_SEED + " TEXT," +
                DbUtils.DbColumns.ENCRYPT_MNEMONIC + " TEXT);");
    }
}
