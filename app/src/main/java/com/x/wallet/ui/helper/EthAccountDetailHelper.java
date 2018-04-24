package com.x.wallet.ui.helper;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.balance.ItemLoadedCallback;
import com.x.wallet.transaction.history.HistoryLoaderManager;
import com.x.wallet.ui.ActionUtils;
import com.x.wallet.ui.adapter.TransactionAdapter;
import com.x.wallet.ui.view.TransactionListItem;

/**
 * Created by wuliang on 18-4-24.
 */

public class EthAccountDetailHelper {
    private static final int TX_LIST_LOADER = 1;

    private Activity mActivity;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TransactionAdapter mAdapter;
    private LoaderManager mLoaderManager;

    private String mAccountAddress;
    private boolean mIsToken;
    private String mContractAddress;

    private OnDataLoadFinishedListener mOnDataLoadFinishedListener;

    public void initEthAccount(Activity activity, LoaderManager loaderManager, String accountAddress, boolean isToken, String contractAddress,
                               OnDataLoadFinishedListener onDataLoadFinishedListener){
        mActivity = activity;
        mLoaderManager = loaderManager;
        mAccountAddress = accountAddress;
        mIsToken = isToken;
        mContractAddress = contractAddress;
        mOnDataLoadFinishedListener = onDataLoadFinishedListener;
        initSwipeRefreshView();
        initRecyclerView();
        initLoaderManager();
    }

    private void initSwipeRefreshView(){
        SwipeRefreshLayout view = (SwipeRefreshLayout)AppUtils.getStubView(mActivity, R.id.transaction_list_view_stub, R.id.layout_swipe_refresh);
        view.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout = view;
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        mOnRefreshListener.onRefresh();
    }

    private void initRecyclerView(){
        mAdapter = new TransactionAdapter(mActivity, null, R.layout.transaction_list_item, mAccountAddress,
                mIsToken,
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        TransactionListItem listItem = (TransactionListItem)v;
                        ActionUtils.openTransactionDetail(mActivity, listItem.getTransactionItem(), mIsToken, LibUtils.COINTYPE.COIN_ETH);
                    }
                });
        mRecyclerView = mActivity.findViewById(R.id.recyclerView);
        final LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
    }

    private void initLoaderManager(){
        Bundle data = new Bundle();
        data.putBoolean("isToken", mIsToken);
        if (mIsToken) {
            data.putString("contract_address", mContractAddress);
        }
        mLoaderManager.initLoader(
                TX_LIST_LOADER,
                data,
                new TxListLoaderCallbacks());
    }

    private void requestHistory(ItemLoadedCallback<HistoryLoaderManager.HistoryLoaded> callback){
        if(mIsToken){
            XWalletApplication.getApplication().getHistoryLoaderManager().getTokenHistory(mAccountAddress, mContractAddress, callback);
        } else {
            XWalletApplication.getApplication().getHistoryLoaderManager().getNormalHistory(mAccountAddress, callback);
        }
        XWalletApplication.getApplication().getBalanceLoaderManager().getAllBalance(null, false);
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            requestHistory(createItemLoadedCallback());
        }
    };

    private ItemLoadedCallback createItemLoadedCallback(){
        return new ItemLoadedCallback<HistoryLoaderManager.HistoryLoaded>() {
            @Override
            public void onItemLoaded(HistoryLoaderManager.HistoryLoaded result, Throwable exception) {
                if (mSwipeRefreshLayout != null){
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        };
    }

    public void destory() {
        if(mLoaderManager != null){
            mLoaderManager.destroyLoader(TX_LIST_LOADER);
        }
    }

    private class TxListLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        final String mOrder = DbUtils.TxTableColumns.TIME_STAMP + " DESC";

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            boolean isToken = bundle.getBoolean("isToken");
            if(isToken){
                String contractAddress = bundle.getString("contract_address");
                return createTokenTxListLoader(contractAddress);
            } else {
                return createEthTxListLoader();
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            AppUtils.log("AccountDetailActivity onLoadFinished cursor.count = " + data.getCount());
            if(mOnDataLoadFinishedListener != null){
                mOnDataLoadFinishedListener.onDataLoadFinished(data.getCount());
            }
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }

        private CursorLoader createEthTxListLoader(){
            String selectionNormal = DbUtils.TxTableColumns.FROM_ADDRESS + " = ? OR (" + DbUtils.TxTableColumns.TO_ADDRESS + " = ? AND " + DbUtils.TxTableColumns.CONTRACT_ADDRESS + " = ? )";
            return new CursorLoader(mActivity, XWalletProvider.CONTENT_URI_TRANSACTION, null, selectionNormal, new String[]{mAccountAddress, mAccountAddress, ""}, mOrder);
        }

        private CursorLoader createTokenTxListLoader(String contractAddress){
            String baseSelection = DbUtils.TxTableColumns.FROM_ADDRESS + " = ? OR " + DbUtils.TxTableColumns.TO_ADDRESS + " = ?";
            String selectionToken = "( " + baseSelection + " )" + " AND " + DbUtils.TxTableColumns.CONTRACT_ADDRESS + " = ?";
            return new CursorLoader(mActivity, XWalletProvider.CONTENT_URI_TRANSACTION, null, selectionToken,
                    new String[]{mAccountAddress, mAccountAddress, contractAddress}, mOrder);
        }
    }

    public interface OnDataLoadFinishedListener{
        void onDataLoadFinished(int count);
    }
}
