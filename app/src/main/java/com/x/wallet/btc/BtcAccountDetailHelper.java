package com.x.wallet.btc;

import android.app.LoaderManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.support.v7.util.AsyncListUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.x.wallet.XWalletApplication;
import com.x.wallet.lib.btc.BtcLibHelper;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.ui.ActionUtils;
import com.x.wallet.ui.data.TransactionItem;
import com.x.wallet.ui.view.TransactionListItem;

import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.utils.Utils;

import java.util.List;

/**
 * Created by wuliang on 18-4-13.
 */

public class BtcAccountDetailHelper {
    private Context mContext;
    private String mAddress;

    private BtcAccountBalanceLoaderHelper mBtcAccountBalanceLoaderHelper;
    private OnDataLoadFinishedListener mOnDataLoadFinishedListener;

    private RecyclerView mRecyclerView;
    private BtcTransactionListAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private AsyncListUtil<Tx> mAsyncListUtil;
    private int mAllTxCount = 0;

    public BtcAccountDetailHelper(Context context) {
        mContext = context;
    }

    public void init(String address, RecyclerView recyclerView, LoaderManager loaderManager,
                     OnDataLoadFinishedListener onDataLoadFinishedListener){
        mAddress = address;
        mOnDataLoadFinishedListener = onDataLoadFinishedListener;

        mRecyclerView = recyclerView;
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        initAsyncListUtil();

        mAdapter = new BtcTransactionListAdapter(address, mAsyncListUtil, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransactionListItem listItem = (TransactionListItem)view;
                Tx tx = listItem.getTx();
                TransactionItem item = new TransactionItem();
                String outAddress = tx.getFirstOutAddress();
                item.setToAddress(outAddress);
                item.setFromAddress(tx.getFromAddress());
                item.setAmount(Long.toString(tx.amountSentToAddress(outAddress)));
                long fee =tx.getFee();
                item.setTransactionFee(fee == Long.MAX_VALUE ? "0" : Long.toString(fee));
                item.setReceiptHash( Utils.hashToString(tx.getTxHash()));
                long amount = BtcLibHelper.deltaAmountFrom(mAddress, tx);
                item.setTransactionType(amount > 0 ? TransactionItem.TRANSACTION_TYPE_RECEIVE : TransactionItem.TRANSACTION_TYPE_TRANSFER_OUT);
                ActionUtils.openTransactionDetail(mContext, item, false, LibUtils.COINTYPE.COIN_BTC);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mAsyncListUtil.onRangeChanged();
            }
        });

        mBtcAccountBalanceLoaderHelper = new BtcAccountBalanceLoaderHelper(mContext, loaderManager, address, new BtcAccountBalanceLoaderHelper.OnDataLoadFinishedListener() {
            @Override
            public void onBalanceLoadFinished(String balance) {
                if(mOnDataLoadFinishedListener != null){
                    mOnDataLoadFinishedListener.onBalanceLoadFinished(balance);
                }
            }
        });

        mBtcAccountBalanceLoaderHelper.forceLoad();

        registerContentObserver();
    }

    public void destory(){
        if(mBtcAccountBalanceLoaderHelper != null){
            mBtcAccountBalanceLoaderHelper.destory();
        }
        unregisterContentObserver();
    }

    private void updateViewVisibility(int size){
        if(size > 0){
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
        }
        mOnDataLoadFinishedListener.onTransationListLoadFinished(size);
    }

    private void initAsyncListUtil(){
        mAllTxCount = BtcLibHelper.getTxsCount(mAddress);
        updateViewVisibility(mAllTxCount);
        MyViewCallback mViewCallback = new MyViewCallback();
        MyDataCallback mDataCallback = new MyDataCallback();
        mAsyncListUtil = new AsyncListUtil<>(Tx.class, BitherjSettings.TX_PAGE_SIZE, mDataCallback, mViewCallback);
    }

    private class MyDataCallback extends AsyncListUtil.DataCallback<Tx> {
        @Override
        public int refreshData() {
            return mAllTxCount;
        }

        @Override
        public void fillData(Tx[] data, int startPosition, int itemCount) {
            List<Tx> list = BtcLibHelper.getTxs(mAddress, startPosition);
            if(list != null){
                for (int i = 0; i < list.size(); i++) {
                    data[i] = list.get(i);
                }
            }
        }
    }

    private class MyViewCallback extends AsyncListUtil.ViewCallback {
        @Override
        public void getItemRangeInto(int[] outRange) {
            getOutRange(outRange);
            if (outRange[0] == -1 && outRange[1] == -1) {
                outRange[0] = 0;
                outRange[1] = getInitOutRange();
            }
        }

        @Override
        public void onDataRefresh() {
            mAdapter.notifyDataSetChanged();
            //mAdapter.notifyItemRangeChanged(mLinearLayoutManager.findFirstVisibleItemPosition(), BitherjSettings.TX_PAGE_SIZE);
        }

        @Override
        public void onItemLoaded(int position) {
            mAdapter.notifyItemChanged(position);
        }

        private void getOutRange(int[] outRange){
            outRange[0] = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            outRange[1] = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
        }

        private int getInitOutRange(){
            if(mAllTxCount > 0){
                return mAllTxCount > BitherjSettings.TX_PAGE_SIZE ? (BitherjSettings.TX_PAGE_SIZE - 1) : mAllTxCount -1;
            }
            return mAllTxCount;
        }
    }

    private void updateAllTxCount(){
        mAllTxCount = BtcLibHelper.getTxsCount(mAddress);
        updateViewVisibility(mAllTxCount);
    }

    private void registerContentObserver(){
        XWalletApplication.getApplication().getContentResolver().registerContentObserver(BtcUtils.BTC_CONTENT_URI, true,
                mTxChangeObserver);
    }

    public void unregisterContentObserver(){
        XWalletApplication.getApplication().getContentResolver().unregisterContentObserver(mTxChangeObserver);
    }

    private final ContentObserver mTxChangeObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfUpdate) {
                    updateAllTxCount();
                    mBtcAccountBalanceLoaderHelper.forceLoad();
                    mAsyncListUtil.refresh();
                }
            };

    public interface OnDataLoadFinishedListener{
        void onBalanceLoadFinished(String balance);
        void onTransationListLoadFinished(int count);
    }
}
