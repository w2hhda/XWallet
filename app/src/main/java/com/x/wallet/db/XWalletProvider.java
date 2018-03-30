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
    public static final String AUTHORITY = "com.x.wallet";
    public static final Uri CONTENT_URI = Uri.parse("content://com.x.wallet/account");
    public static final Uri CONTENT_URI_TOKEN = Uri.parse("content://com.x.wallet/token");
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
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
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
        }
        if(result != null){
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
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
        }
        if(count > 0){
            getContext().getContentResolver().notifyChange(uri, null);
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
        }
        Log.i(AppUtils.APP_TAG, "XWalletProvider update count =" + count);
        if(count > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    private static final int URI_ACCOUNT                     = 0;
    private static final int URI_ACCOUNT_ID                  = 1;
    private static final int URI_TOKEN                       = 2;
    static {
        URI_MATCHER.addURI(AUTHORITY, "account", URI_ACCOUNT);
        URI_MATCHER.addURI(AUTHORITY, "account/#", URI_ACCOUNT_ID);
        URI_MATCHER.addURI(AUTHORITY, "token", URI_TOKEN);
    }
}
