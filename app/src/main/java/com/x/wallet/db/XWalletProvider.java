package com.x.wallet.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.x.wallet.AppUtils;

/**
 * Created by wuliang on 18-3-14.
 */

public class XWalletProvider extends ContentProvider {

    static final String TABLE_ACCOUNT = "account";
    static final String TABLE_TOKEN = "token";
    static final String TABLE_TRANSACTION = "txlists";
    public static final String AUTHORITY = "com.x.wallet";
    public static final Uri RAW_CONTENT_URI = Uri.parse("content://com.x.wallet/");
    public static final Uri ALL_ACCOUNT_CONTENT_URI = Uri.parse("content://com.x.wallet/allaccount");
    public static final Uri CONTENT_URI = Uri.parse("content://com.x.wallet/account");
    public static final Uri CONTENT_URI_TOKEN = Uri.parse("content://com.x.wallet/token");
    public static final Uri CONTENT_URI_TRANSACTION = Uri.parse("content://com.x.wallet/txlists");

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private SQLiteOpenHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = WalletDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = null;
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_ACCOUNT:
                cursor = db.query(TABLE_ACCOUNT,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            case URI_ACCOUNT_ID:
                cursor = db.query(TABLE_ACCOUNT,
                        projection,
                        DbUtils.DbColumns._ID + "=" + uri.getLastPathSegment(),
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            case URI_TOKEN:
                cursor = db.query(TABLE_TOKEN,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            case ALL_ACCOUNT:
                String sql = "SELECT " + createMainAccountSql() + " FROM " + TABLE_ACCOUNT + " as t1"
                        + " UNION "
                        + "SELECT " + createTokenSql() + " FROM " + TABLE_TOKEN + " as t2"
                        + " order by _id,all_coin_type";
                cursor = db.rawQuery(sql, selectionArgs);
                break;
            case URI_TRANSACTION:
                cursor = db.query(TABLE_TRANSACTION,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i(AppUtils.APP_TAG, "XWalletProvider insert uri = " + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri result = null;
        int match = URI_MATCHER.match(uri);
        switch (match){
            case URI_ACCOUNT:
                long rowId = db.insert(TABLE_ACCOUNT, null, values);
                Log.i(AppUtils.APP_TAG, "XWalletProvider insert rowId = " + rowId);
                result = Uri.parse(uri + "/" + rowId);
                break;
            case URI_TOKEN:
                long rowId2 = db.insert(TABLE_TOKEN, null, values);
                Log.i(AppUtils.APP_TAG, "XWalletProvider insert rowId = " + rowId2);
                result = Uri.parse(uri + "/" + rowId2);
            break;
            case URI_TRANSACTION:
                long rowId3 = db.insert(TABLE_TRANSACTION, null, values);
                Log.i(AppUtils.APP_TAG, "XWalletProvider insert rowId = " + rowId3);
                result = Uri.parse(uri + "/" + rowId3);
            break;
        }
        if(result != null){
            //getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(RAW_CONTENT_URI, null);
        }
        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        switch (match){
            case URI_ACCOUNT:
                break;
            case URI_ACCOUNT_ID:
                count = db.delete(TABLE_ACCOUNT, DbUtils.DbColumns._ID + "=" + uri.getLastPathSegment(), null);
                break;
            case URI_TOKEN:
                count = db.delete(TABLE_TOKEN, selection, selectionArgs);
                break;
            case URI_TRANSACTION:
                count = db.delete(TABLE_TRANSACTION, selection, selectionArgs);
                break;
        }
        if(count > 0){
            //getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(RAW_CONTENT_URI, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        int count = 0;
        switch (match){
            case URI_ACCOUNT:
                count = db.update(TABLE_ACCOUNT, values, selection, selectionArgs);
                break;
            case URI_ACCOUNT_ID:
                count = db.update(TABLE_ACCOUNT, values, DbUtils.DbColumns._ID + "=" + uri.getLastPathSegment(), null);
                break;
            case URI_TOKEN:
                count = db.update(TABLE_TOKEN, values, selection, selectionArgs);
                break;
            case URI_TRANSACTION:
                count = db.update(TABLE_TRANSACTION, values, selection, selectionArgs);
                break;
        }
        Log.i(AppUtils.APP_TAG, "XWalletProvider update count =" + count);
        if(count > 0){
            //getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(RAW_CONTENT_URI, null);
        }
        return count;
    }

    private static String createMainAccountSql(){
        return "t1._id as _id" + ", " +
                "t1._id as self_id" + ", " +
                "t1.address as address" + ", " +
                "t1.name as name " + ", " +
                "t1.coin_name as coin_name " + ", " +
                "t1.coin_type as coin_type" + ", " +
                "t1." + DbUtils.DbColumns.ENCRYPT_SEED + " as encrypt_seed, " +
                "t1." + DbUtils.DbColumns.ENCRYPT_MNEMONIC + " as " + DbUtils.DbColumns.ENCRYPT_MNEMONIC + ", " +
                "t1." + DbUtils.DbColumns.KEYSTORE + " as " + DbUtils.DbColumns.KEYSTORE + ", " +
                "t1." + DbUtils.DbColumns.BALANCE + " as " + DbUtils.DbColumns.BALANCE + ", " +
                "0 as " + DbUtils.TokenTableColumns.DECIMALS + ", " +
                "0 as " + DbUtils.TokenTableColumns.RATE + ", " +
                "null as " + DbUtils.TokenTableColumns.CONTRACT_ADDRESS + ", " +
                "-1 as " + DbUtils.TokenTableColumns.ID_IN_ALL + ", " +
                "1 as all_coin_type";
    }

    private static String createTokenSql(){
        return "t2." + DbUtils.TokenTableColumns.ACCOUNT_ID + " as _id, " +
                "t2." + DbUtils.TokenTableColumns._ID + " as self_id, " +
                "t2." + DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " as address, " +
                "null as " + DbUtils.DbColumns.NAME + ", " +
                "t2." + DbUtils.TokenTableColumns.SYMBOL + " as " + DbUtils.DbColumns.COIN_NAME + ", " +
                "-1 as " + DbUtils.DbColumns.COIN_TYPE + ", " +
                "null as " + DbUtils.DbColumns.ENCRYPT_SEED + ", " +
                "null as " + DbUtils.DbColumns.ENCRYPT_MNEMONIC + ", " +
                "null as " + DbUtils.DbColumns.KEYSTORE + ", " +
                "t2." + DbUtils.TokenTableColumns.BALANCE + " as " + DbUtils.DbColumns.BALANCE + ", " +
                "t2." + DbUtils.TokenTableColumns.DECIMALS + " as " + DbUtils.TokenTableColumns.DECIMALS + ", " +
                "t2." + DbUtils.TokenTableColumns.RATE + " as " + DbUtils.TokenTableColumns.RATE + ", " +
                "t2." + DbUtils.TokenTableColumns.CONTRACT_ADDRESS + " as " + DbUtils.TokenTableColumns.CONTRACT_ADDRESS + ", " +
                "t2." + DbUtils.TokenTableColumns.ID_IN_ALL + " as " + DbUtils.TokenTableColumns.ID_IN_ALL + ", " +
                "2 as all_coin_type";
    }

    private static final int URI_ACCOUNT                     = 0;
    private static final int URI_ACCOUNT_ID                  = 1;
    private static final int URI_TOKEN                       = 2;
    private static final int ALL_ACCOUNT                     = 3;
    private static final int URI_TRANSACTION                 = 4;
    static {
        URI_MATCHER.addURI(AUTHORITY, "account", URI_ACCOUNT);
        URI_MATCHER.addURI(AUTHORITY, "account/#", URI_ACCOUNT_ID);
        URI_MATCHER.addURI(AUTHORITY, "token", URI_TOKEN);
        URI_MATCHER.addURI(AUTHORITY, "allaccount", ALL_ACCOUNT);
        URI_MATCHER.addURI(AUTHORITY, "txlists", URI_TRANSACTION);
    }
}
