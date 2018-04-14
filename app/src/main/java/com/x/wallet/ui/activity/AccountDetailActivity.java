package com.x.wallet.ui.activity;


import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.transaction.balance.BalanceConversionUtils;
import com.x.wallet.transaction.balance.ItemLoadedCallback;
import com.x.wallet.transaction.history.HistoryLoaderManager;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;
import com.x.wallet.ui.adapter.TransactionAdapter;
import com.x.wallet.ui.data.RawAccountItem;
import com.x.wallet.ui.data.SerializableAccountItem;
import com.x.wallet.ui.data.TransactionItem;
import com.x.wallet.ui.view.TransactionListItem;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountDetailActivity extends WithBackAppCompatActivity {
    private SerializableAccountItem mAccountItem;
    private RawAccountItem mTokenItem;

    private TextView mBalanceTranslateTv;
    private TextView mBalanceTv;
    private TextView mAddressTv;
    private View mSendOutBtn;
    private View mReceiptBtn;
    private View mNoTransactionView;
    private RecyclerView mRecyclerView;
    private MyHandler handler;
    private SwipeRefreshLayout refreshLayout;
    private TransactionAdapter mAdapter;
    private LoaderManager mLoaderManager;

    private static final int TX_LIST_LOADER = 1;
    private static final int TX_TOKEN_LIST_LOADER = 2;
    private static final String NORMAL_REQUEST = "normal_request";
    private static final String TOKEN_REQUEST = "token_request";
    private static final String ALL_REQUEST = "all_request";


    public final static String SHARE_ADDRESS_EXTRA = "share_address_extra";
    private BalanceConversionUtils.RateUpdateListener mRateUpdateListener;
    private Boolean isTokenAccount = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_detail_activity);

        if (getIntent().hasExtra(AppUtils.ACCOUNT_DATA)) {
            mAccountItem = (SerializableAccountItem) getIntent().getSerializableExtra(AppUtils.ACCOUNT_DATA);
        }
        if (getIntent().hasExtra(AppUtils.TOKEN_DATA)) {
            mTokenItem = (RawAccountItem) getIntent().getSerializableExtra(AppUtils.TOKEN_DATA);
            isTokenAccount = true;
        }

        initViews();
    }

    private void initViews(){
        mBalanceTranslateTv = findViewById(R.id.balance_translate_tv);
        mBalanceTv = findViewById(R.id.balance_tv);
        mAddressTv = findViewById(R.id.address_tv);
        mSendOutBtn = findViewById(R.id.send_btn);
        mReceiptBtn = findViewById(R.id.receipt_btn);
        mNoTransactionView = findViewById(R.id.no_transaction_view);
        refreshLayout = findViewById(R.id.layout_swipe_refresh);
        super.setTitle(mAccountItem.getAccountName());
        handler = new MyHandler();
        mAddressTv.setText(getResources().getString(R.string.address) + ": " + mAccountItem.getAddress());

        mReceiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action_SHARE_ADDRESS_QR_ACTION");
                intent.putExtra(SHARE_ADDRESS_EXTRA,mAccountItem.getAddress());
                startActivity(intent);
            }
        });

        mSendOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action_TRANSFER_TO_ADDRESS_ACTION");
                //intent.putExtra(SHARE_ADDRESS_EXTRA, mAccountItem.getAddress());

                intent.putExtra(AppUtils.ACCOUNT_DATA, mAccountItem);
                if (isTokenAccount){
                    //intent.putExtra(TOKEN_EXTRA, mTokenItem);
                    intent.putExtra(AppUtils.TOKEN_DATA, mTokenItem);
                }
                startActivity(intent);
            }
        });

        mAddressTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", mAccountItem.getAddress());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(AccountDetailActivity.this, R.string.has_copied_address, Toast.LENGTH_SHORT).show();
            }
        });
        initRecyclerView();
        initViewForNormal();
        initSwipeRefreshView();
        initLoaderManager(isTokenAccount);

    }

    private SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            requestHistory(createItemLoadedCallback(), isTokenAccount ? TOKEN_REQUEST : NORMAL_REQUEST);
        }
    };

    private void initViewForNormal(){

        if (isTokenAccount){
            mBalanceTv.setText(TokenUtils.getBalanceText(mTokenItem.getBalance(), mTokenItem.getDecimals()) + " " + mTokenItem.getCoinName());
            mBalanceTranslateTv.setText(this.getString(R.string.item_balance, UsdToCnyHelper.getChooseCurrencyUnit(),
                    TokenUtils.getTokenConversionText(mTokenItem.getBalance(), mTokenItem.getDecimals(), mTokenItem.getRate())));
        }else {
            updateBalanceConversionText();
            mBalanceTv.setText(TokenUtils.getBalanceText(mAccountItem.getBalance(), TokenUtils.ETH_DECIMALS) + " ETH");
        }
        if(mRateUpdateListener != null){
            BalanceConversionUtils.unRegisterListener(mRateUpdateListener);
        }
        mRateUpdateListener = new BalanceConversionUtils.RateUpdateListener() {
            @Override
            public void onRateUpdate() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateBalanceConversionText();
                    }
                });
            }
        };
        BalanceConversionUtils.registerListener(mRateUpdateListener);
    }

    private void initSwipeRefreshView(){
        refreshLayout.setOnRefreshListener(listener);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        });
        listener.onRefresh();
    }

    private void initRecyclerView(){
        mAdapter = new TransactionAdapter(this, null, R.layout.transaction_list_item, mAccountItem.getAddress() ,
                isTokenAccount,
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        TransactionListItem listItem = (TransactionListItem)v;
                        TransactionItem item = listItem.getmTransactionItem();
                        Intent intent = new Intent("com.x.wallet.action_SEE_TRANSACTION_DETAIL");
                        intent.putExtra(AppUtils.TRANSACTION_ITEM, item);
                        startActivity(intent);
                    }
                });
        mRecyclerView = findViewById(R.id.recyclerView);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    private void initLoaderManager(boolean isTokenAccount){
        mLoaderManager = getLoaderManager();
        Loader tokenListLoader;
        if (!isTokenAccount) {
            tokenListLoader = getLoaderManager().initLoader(
                    TX_LIST_LOADER,
                    new Bundle(),
                    new AccountDetailActivity.NormalLoaderCallbacks());
        }else {
            Bundle data = new Bundle();
            data.putString("tokenAddress", mTokenItem.getContractAddress());
            tokenListLoader = getLoaderManager().initLoader(
                    TX_TOKEN_LIST_LOADER,
                    data,
                    new AccountDetailActivity.NormalLoaderCallbacks());
        }
        tokenListLoader.forceLoad();
    }

    private class NormalLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        final String baseSelection = DbUtils.TxTableColumns.FROM_ADDRESS + " = ? OR " + DbUtils.TxTableColumns.TO_ADDRESS + " = ?";
        final String order = DbUtils.TxTableColumns.TIME_STAMP + " DESC";
        final String selectionToken = "( " + baseSelection + " )" + " AND " + DbUtils.TxTableColumns.CONTRACT_ADDRESS + " = ?";
        //from and " to & exclude contract address "
        final String selectionNormal = DbUtils.TxTableColumns.FROM_ADDRESS + " = ? OR (" + DbUtils.TxTableColumns.TO_ADDRESS + " = ? AND " + DbUtils.TxTableColumns.CONTRACT_ADDRESS + " = ? )";
        final String address = mAccountItem.getAddress();
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            if (id == TX_LIST_LOADER) {
                return new CursorLoader(AccountDetailActivity.this, XWalletProvider.CONTENT_URI_TRANSACTION, null, selectionNormal, new String[]{address, address, ""}, order);
            }else {
                String token = bundle.getString("tokenAddress");
                AppUtils.log(selectionToken + "address = " + token);
                return new CursorLoader(AccountDetailActivity.this, XWalletProvider.CONTENT_URI_TRANSACTION, null, selectionToken,
                        new String[]{address, address, token}, order);
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            AppUtils.log("AccountDetailActivity onLoadFinished cursor.count = " + data.getCount());
            updateVisibility(data.getCount());
            requestHistory(createItemLoadedCallback(), ALL_REQUEST);
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private ItemLoadedCallback createItemLoadedCallback(){
        return new ItemLoadedCallback<HistoryLoaderManager.HistoryLoaded>() {
            @Override
            public void onItemLoaded(HistoryLoaderManager.HistoryLoaded result, Throwable exception) {
                if (refreshLayout != null){
                    refreshLayout.setRefreshing(false);
                }
                //handler.sendEmptyMessage(MyHandler.MSG_UPDATE);
            }
        };
    }

    private void requestHistory(ItemLoadedCallback<HistoryLoaderManager.HistoryLoaded> callback1,
                                String requestType){
        if (requestType.equals(NORMAL_REQUEST)) {
            XWalletApplication.getApplication().getmHistoryLoaderManager().getTokenHistory(mAccountItem.getAddress(), callback1);
        }else if (requestType.equals(TOKEN_REQUEST)) {
            XWalletApplication.getApplication().getmHistoryLoaderManager().getNormalHistory(mAccountItem.getAddress(), callback1);
        }else{
            XWalletApplication.getApplication().getmHistoryLoaderManager().getTokenHistory(mAccountItem.getAddress(), callback1);
            XWalletApplication.getApplication().getmHistoryLoaderManager().getNormalHistory(mAccountItem.getAddress(), callback1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoaderManager != null){
            mLoaderManager.destroyLoader(TX_LIST_LOADER);
            mLoaderManager.destroyLoader(TX_TOKEN_LIST_LOADER);
            mLoaderManager = null;
        }
        BalanceConversionUtils.clearListener();
        TokenUtils.setRateUpdateListener(null);
    }

    private class MyHandler extends Handler{
        public static final int MSG_UPDATE = 101;
        public static final int MSG_LOAD_DONE = 102;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(AppUtils.APP_TAG,"msg.what = " + msg.what);
            switch (msg.what){
                case MSG_UPDATE:
                    mNoTransactionView.setVisibility(View.GONE);
                    refreshLayout.setRefreshing(false);
                    break;
                default:
                        break;
            }
        }
    }

    private void updateVisibility(int count){
        if (count > 0){
            mNoTransactionView.setVisibility(View.GONE);
        }else {
            mNoTransactionView.setVisibility(View.VISIBLE);
        }
    }

    public void updateBalanceConversionText(){
        if(mAccountItem != null){
            mBalanceTranslateTv.setText(getResources().getString(R.string.item_balance, UsdToCnyHelper.getChooseCurrencyUnit(),
                    TokenUtils.getBalanceConversionText(mAccountItem.getBalance(), TokenUtils.ETH_DECIMALS)));
        }
    }

}
