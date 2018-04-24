package com.x.wallet.transaction.balance;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import com.x.wallet.AppUtils;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;

/**
 * Created by wuliang on 18-4-17.
 */

public class AccountBalanceLoaderHelper {
    public static final int ACCOUNT_BALANCE_LOADER = 2;
    private Context mContext;
    private LoaderManager mLoaderManager;
    private AccountBalanceLoaderCallbacks mAccountBalanceLoaderCallbacks;
    private OnDataLoadFinishedListener mOnDataLoadFinishedListener;

    public AccountBalanceLoaderHelper(Context context, LoaderManager loaderManager, String address, String contractAddress,
                                      OnDataLoadFinishedListener onDataLoadFinishedListener) {
        mContext = context;
        mLoaderManager = loaderManager;
        mOnDataLoadFinishedListener = onDataLoadFinishedListener;
        mAccountBalanceLoaderCallbacks = new AccountBalanceLoaderCallbacks();
        final Bundle args = new Bundle();
        args.putString(AppUtils.ACCOUNT_ADDRESS, address);
        args.putString("contract_address", contractAddress);
        mLoaderManager.initLoader(ACCOUNT_BALANCE_LOADER, args, mAccountBalanceLoaderCallbacks);
    }

    public AccountBalanceLoaderHelper(Context context, LoaderManager loaderManager, String address,
                                      OnDataLoadFinishedListener onDataLoadFinishedListener) {
        mContext = context;
        mLoaderManager = loaderManager;
        mOnDataLoadFinishedListener = onDataLoadFinishedListener;
        mAccountBalanceLoaderCallbacks = new AccountBalanceLoaderCallbacks();
        final Bundle args = new Bundle();
        args.putString(AppUtils.ACCOUNT_ADDRESS, address);
        mLoaderManager.initLoader(ACCOUNT_BALANCE_LOADER, args, mAccountBalanceLoaderCallbacks);
    }

    private class AccountBalanceLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if(args.containsKey("contract_address")){
                return new CursorLoader(mContext, XWalletProvider.CONTENT_URI_TOKEN,  new String[]{DbUtils.DbColumns.BALANCE},
                        DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " = ? AND " + DbUtils.TokenTableColumns.CONTRACT_ADDRESS + " = ?",
                        new String[]{args.getString(AppUtils.ACCOUNT_ADDRESS), args.getString("contract_address")},
                        null);
            } else {
                return new CursorLoader(mContext, XWalletProvider.CONTENT_URI,  new String[]{DbUtils.DbColumns.BALANCE},
                        DbUtils.ADDRESS_SELECTION,
                        new String[]{args.getString(AppUtils.ACCOUNT_ADDRESS)},
                        null);
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if(mOnDataLoadFinishedListener != null && cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                String balance = cursor.getString(0);
                mOnDataLoadFinishedListener.onBalanceLoadFinished(balance);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    public interface OnDataLoadFinishedListener{
        void onBalanceLoadFinished(String balance);
    }
}
