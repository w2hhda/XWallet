package com.x.wallet.btc;

import android.app.LoaderManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.x.wallet.lib.btc.BtcLibHelper;

import net.bither.bitherj.core.Tx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wuliang on 18-4-13.
 */

public class BtcAccountDetailHelper {
    private Context mContext;
    private String mAddress;

    private BtcAccountBalanceLoaderHelper mBtcAccountBalanceLoaderHelper;
    private OnDataLoadFinishedListener mOnDataLoadFinishedListener;

    private ListView mListView;
    private BtcTransactionListAdapter mAdapter;
    private int page = 1;
    private ArrayList<Tx> transactions = new ArrayList<Tx>();
    private boolean hasMore = true;
    private boolean isLoding = false;

    public BtcAccountDetailHelper(Context context) {
        mContext = context;
    }

    public void init(String address, ListView listView, LoaderManager loaderManager,
                     OnDataLoadFinishedListener onDataLoadFinishedListener){
        mAddress = address;
        mListView = listView;
        mOnDataLoadFinishedListener = onDataLoadFinishedListener;
        mAdapter = new BtcTransactionListAdapter(mContext, address, transactions);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int lastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem + visibleItemCount >= totalItemCount - 6
                        && hasMore && !isLoding
                        && lastFirstVisibleItem < firstVisibleItem) {
                    page++;
                    loadTx();
                }
                lastFirstVisibleItem = firstVisibleItem;
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

        loadData();
    }

    private void loadData() {
        page = 1;
        hasMore = true;
        loadTx();
        mBtcAccountBalanceLoaderHelper.forceLoad();
    }

    private void loadTx() {
        if (!isLoding && hasMore) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    isLoding = true;
                    final List<Tx> txs = BtcLibHelper.getTxs(mAddress, page);
                    if(txs != null){
                        Log.i("testBtcDetail", "BtcAccountDetailHelper loadTx size = " + txs.size());
                    } else {
                        Log.i("testBtcDetail", "BtcAccountDetailHelper loadTx tx is null!");
                    }
                    mListView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (page == 1) {
                                transactions.clear();
                            }
                            if (txs != null && txs.size() > 0) {
                                transactions.addAll(txs);
                                hasMore = true;
                            } else {
                                hasMore = false;
                            }
                            if(transactions.size() > 0){
                                mListView.setVisibility(View.VISIBLE);
                            } else {
                                mListView.setVisibility(View.GONE);
                            }
                            mOnDataLoadFinishedListener.onTransationListLoadFinished(transactions.size());
                            Collections.sort(transactions);
                            mAdapter.notifyDataSetChanged();
                            isLoding = false;
                        }
                    });
                }
            }).start();
        }
    }

    public void destory(){
        if(mBtcAccountBalanceLoaderHelper != null){
            mBtcAccountBalanceLoaderHelper.destory();
        }
    }

    public interface OnDataLoadFinishedListener{
        void onBalanceLoadFinished(String balance);
        void onTransationListLoadFinished(int count);
    }
}
