package com.x.wallet.btc;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import com.x.wallet.transaction.token.TokenUtils;

/**
 * Created by wuliang on 18-4-17.
 */

public class BtcAccountBalanceLoaderHelper {
    private static final String ADDRESS = "address";
    private static final int ACCOUNT_DETAIL_BALANCE_LOADER = 1;
    private Context mContext;
    private LoaderManager mLoaderManager;
    private AccountBalanceLoaderCallbacks mAccountBalanceLoaderCallbacks;
    private BtcAccountBalanceLoader mBtcAccountBalanceLoader;
    private OnDataLoadFinishedListener mOnDataLoadFinishedListener;
    private long mBalance;

    public BtcAccountBalanceLoaderHelper(Context context, LoaderManager loaderManager, String address,
                                         OnDataLoadFinishedListener onDataLoadFinishedListener) {
        mContext = context;
        mLoaderManager = loaderManager;
        mOnDataLoadFinishedListener = onDataLoadFinishedListener;
        mAccountBalanceLoaderCallbacks = new AccountBalanceLoaderCallbacks();
        final Bundle args = new Bundle();
        args.putString(ADDRESS, address);
        mBtcAccountBalanceLoader = (BtcAccountBalanceLoader) mLoaderManager.initLoader(ACCOUNT_DETAIL_BALANCE_LOADER, args, mAccountBalanceLoaderCallbacks);
    }

    private class AccountBalanceLoaderCallbacks implements LoaderManager.LoaderCallbacks<Long> {

        @Override
        public Loader<Long> onCreateLoader(int id, Bundle args) {
            return new BtcAccountBalanceLoader(mContext, args.getString(ADDRESS));
        }

        @Override
        public void onLoadFinished(Loader<Long> loader, Long balance) {
            Log.i("testBtcDetail", "BtcAccountDetailHelper onLoadFinished balance = " + balance);
            mBalance = balance;
            if(mOnDataLoadFinishedListener != null){
                mOnDataLoadFinishedListener.onBalanceLoadFinished(TokenUtils.getBalanceText(balance, BtcUtils.BTC_DECIMALS_COUNT));
            }
        }

        @Override
        public void onLoaderReset(Loader<Long> loader) {

        }
    }

    public void forceLoad(){
        mBtcAccountBalanceLoader.forceLoad();
    }

    public void destory(){
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(ACCOUNT_DETAIL_BALANCE_LOADER);
            mLoaderManager = null;
        }
    }

    public interface OnDataLoadFinishedListener{
        void onBalanceLoadFinished(String balance);
    }
}
