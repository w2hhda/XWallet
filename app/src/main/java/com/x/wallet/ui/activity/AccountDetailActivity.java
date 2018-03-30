package com.x.wallet.ui.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.lib.eth.EthUtils;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.TransactionsResultBean;
import com.x.wallet.transaction.balance.BalanceConversionUtils;
import com.x.wallet.ui.adapter.AccountDetailAdapter;
import com.x.wallet.ui.data.AccountItem;
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

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountDetailActivity extends WithBackAppCompatActivity {
    private AccountItem mAccountItem;

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

    public final static String SHARE_ADDRESS_EXTRA = "share_address_extra";
    private BalanceConversionUtils.RateUpdateListener mRateUpdateListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_detail_activity);

        mAccountItem = (AccountItem) getIntent().getSerializableExtra(AppUtils.ACCOUNT_DATA);
        this.setTitle(mAccountItem.getAccountName());

        items = new  ArrayList<>();
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
        handler = new MyHandler();

        initViews();
    }

    private void initViews(){
        mBalanceTranslateTv = findViewById(R.id.balance_translate_tv);

        mBalanceTv = findViewById(R.id.balance_tv);
        mAddressTv = findViewById(R.id.address_tv);
        mAddressTv.setText(mAccountItem.getAddress());

        mSendOutBtn = findViewById(R.id.send_btn);
        mReceiptBtn = findViewById(R.id.receipt_btn);
        mNoTransactionView = findViewById(R.id.no_transaction_view);
        mRecyclerView = findViewById(R.id.recyclerView);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

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
                intent.putExtra(SHARE_ADDRESS_EXTRA, mAccountItem.getAddress());
                startActivity(intent);
            }
        });

        updateBalanceConversionText();
        BigDecimal balance = new BigDecimal(mAccountItem.getBalance());
        String balanceInEth = EthUtils.translateWeiToEth(balance).stripTrailingZeros().toPlainString();
        mBalanceTv.setText(balanceInEth + "ETH");
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

    @Override
    protected void onResume() {
        super.onResume();
        try {
            //String address = "0xe2258d66b820fc4f0017017373c7b9f742596f27";
            EtherscanAPI.getInstance().getNormalTransactions(mAccountItem.getAddress(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    TransactionsResultBean bean = new Gson().fromJson(response.body().string(), TransactionsResultBean.class);
                    List<TransactionsResultBean.ReceiptBean> receiptBeans = bean.getResult();

                    items.clear();
                    for(TransactionsResultBean.ReceiptBean receiptBean: receiptBeans){
                        Boolean isReceive = true;
                        if (receiptBean.getFrom().equalsIgnoreCase(mAccountItem.getAddress())){
                            isReceive = false;
                        }
                        items.add(TransactionItem.createFromReceipt(receiptBean, isReceive));
                    }

                    if (items.size() > 0) {
                        Message message = handler.obtainMessage();
                        message.what = MyHandler.MSG_UPDATE;
                        handler.sendMessage(message);
                    }


                }
            }, true);
        }catch (IOException e){
            Log.i("@@@@","exception in asyncTask");
        }
    }

    private class MyHandler extends Handler{
        public static final int  MSG_UPDATE = -1;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case -1:
                    mNoTransactionView.setVisibility(View.GONE);
                    adapter.addItems(items);
                    break;
                default:
                        break;
            }
        }
    }

    public void updateBalanceConversionText(){
        if(mAccountItem != null){
            mBalanceTranslateTv.setText(getResources().getString(R.string.item_balance, BalanceConversionUtils.calculateBalanceText(mAccountItem.getBalance())));
        }
    }
}
