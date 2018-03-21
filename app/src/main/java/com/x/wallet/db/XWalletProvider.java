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
    private static final String AUTHORITY = "com.x.wallet";
    public static final Uri CONTENT_URI = Uri.parse("content://com.x.wallet/account");
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
        if (URI_MATCHER.match(uri) == URI_ACCOUNT) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            long rowId = db.insert(TABLE_ACCOUNT, null, values);
            Log.i(AppUtils.APP_TAG, "XWalletProvider insert rowId = " + rowId);
            return Uri.parse(uri + "/" + rowId);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    private static final int URI_ACCOUNT                     = 0;
    static {
        URI_MATCHER.addURI(AUTHORITY, "account", URI_ACCOUNT);
    }
}
