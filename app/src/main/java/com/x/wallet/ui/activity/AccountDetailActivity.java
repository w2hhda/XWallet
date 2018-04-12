package com.x.wallet.ui.activity;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.TransactionsResultBean;
import com.x.wallet.lib.eth.util.CacheHelper;
import com.x.wallet.transaction.balance.BalanceConversionUtils;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;
import com.x.wallet.ui.adapter.AccountDetailAdapter;
import com.x.wallet.ui.data.RawAccountItem;
import com.x.wallet.ui.data.SerializableAccountItem;
import com.x.wallet.ui.data.TransactionItem;
import com.x.wallet.ui.view.TransactionListItem;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
    private AccountDetailAdapter adapter;
    private List<TransactionItem> items;
    private MyHandler handler;
    private WebView webView;
    private SwipeRefreshLayout refreshLayout;
    private SwipeRefreshLayout tokenRefresh;

    public final static String SHARE_ADDRESS_EXTRA = "share_address_extra";
    private BalanceConversionUtils.RateUpdateListener mRateUpdateListener;
    private String CONTRACT_ADDRESS ;
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

        if (isTokenAccount){
            initViewForToken();
        }else {
            initViewForNormal();
        }

    }

    private SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (isTokenAccount){
                //tokenRefresh.setRefreshing(false);
                getTokenTransactions(true);
            }else {
                getNormalTransactions(true);
            }
        }
    };

    private void initViewForNormal(){
        items = new  ArrayList<>();

        refreshLayout.setOnRefreshListener(listener);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        });
        listener.onRefresh();

        adapter = new AccountDetailAdapter(this , new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransactionListItem listItem = (TransactionListItem)view;
                TransactionItem item = listItem.getmTransactionItem();
                Intent intent = new Intent("com.x.wallet.action_SEE_TRANSACTION_DETAIL");
                intent.putExtra(AppUtils.TRANSACTION_ITEM, item);
                startActivity(intent);
            }
        });
        initRecyclerView();
        updateBalanceConversionText();
        mBalanceTv.setText(TokenUtils.getBalanceText(mAccountItem.getBalance(), TokenUtils.ETH_DECIMALS) + " ETH");
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

    private void initRecyclerView(){
        mRecyclerView = findViewById(R.id.recyclerView);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    private void initViewForToken(){
        tokenRefresh = findViewById(R.id.token_refresh);
        tokenRefresh.setVisibility(View.VISIBLE);
        tokenRefresh.setOnRefreshListener(listener);
        if (refreshLayout != null) refreshLayout.setVisibility(View.GONE);
        webView = findViewById(R.id.webView);
        mBalanceTv.setText(TokenUtils.getBalanceText(mTokenItem.getBalance(), mTokenItem.getDecimals()) + " " + mTokenItem.getCoinName());
        mBalanceTranslateTv.setText(this.getString(R.string.item_balance, UsdToCnyHelper.getChooseCurrencyUnit(),
                TokenUtils.getTokenConversionText(mTokenItem.getBalance(), mTokenItem.getDecimals(), mTokenItem.getRate())));

        CONTRACT_ADDRESS = mTokenItem.getContractAddress();
        getTokenTransactions(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTokenAccount){

        }else {
            getNormalTransactions(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                    adapter.addItems(items);
                    refreshLayout.setRefreshing(false);
                    break;
                default:
                        break;
            }
        }
    }

    public void updateBalanceConversionText(){
        if(mAccountItem != null){
            mBalanceTranslateTv.setText(getResources().getString(R.string.item_balance, UsdToCnyHelper.getChooseCurrencyUnit(),
                    TokenUtils.getBalanceConversionText(mAccountItem.getBalance(), TokenUtils.ETH_DECIMALS)));
        }
    }

    private void getNormalTransactions(Boolean force){
        try {
            //String address = "0xe2258d66b820fc4f0017017373c7b9f742596f27";
            EtherscanAPI.getInstance().getNormalTransactions(mAccountItem.getAddress(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    ResponseBody body = response.body();
                    if(body != null){
                        String result = body.string();
                        CacheHelper.instance().put(CacheHelper.TYPE_TXS_NORMAL, mAccountItem.getAddress(), result);
                        TransactionsResultBean bean = new Gson().fromJson(result, TransactionsResultBean.class);
                        List<TransactionsResultBean.ReceiptBean> receiptBeans = bean.getResult();

                        items.clear();
                        for(TransactionsResultBean.ReceiptBean receiptBean: receiptBeans){
                            Boolean isReceive = true;
                            Boolean isToken = false;
                            if (receiptBean.getFrom().equalsIgnoreCase(mAccountItem.getAddress())){
                                isReceive = false;
                            }
                            if (receiptBean.getInput() != null && receiptBean.getInput().length() > 10 && new BigInteger(receiptBean.getValue()).equals(BigInteger.ZERO)){ //input contains token transaction info
                                isToken = true;
                            }
                            items.add(TransactionItem.createFromReceipt(receiptBean, isReceive, isToken));
                        }

                        if (items.size() > 0) {
                            Message message = handler.obtainMessage();
                            message.what = MyHandler.MSG_UPDATE;
                            handler.sendMessage(message);
                        }
                    }
                }
            }, force);

        }catch (IOException e){
            Log.i(AppUtils.APP_TAG,"exception in getNormalTransactions asyncTask");
        }
    }

    private void getTokenTransactions(Boolean force){
        if (new BigDecimal(mTokenItem.getBalance()).compareTo(BigDecimal.ZERO) > 0) {
            mNoTransactionView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        final String url = "https://etherscan.io/token/generic-tokentxns2?contractAddress=" + CONTRACT_ADDRESS + "&a=" + mAccountItem.getAddress();
        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (tokenRefresh.isRefreshing()){
                    tokenRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (tokenRefresh != null){
                    tokenRefresh.setRefreshing(true);
                }
            }
        });

    }
}
