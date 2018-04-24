package com.x.wallet.ui.activity;


import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.btc.BtcAccountDetailHelper;
import com.x.wallet.btc.BtcUtils;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.balance.AccountBalanceLoaderHelper;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;
import com.x.wallet.ui.data.RawAccountItem;
import com.x.wallet.ui.data.SerializableAccountItem;
import com.x.wallet.ui.helper.EthAccountDetailHelper;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountDetailActivity extends WithBackAppCompatActivity {
    private SerializableAccountItem mAccountItem;
    private RawAccountItem mTokenItem;

    private TextView mBalanceTranslateTv;
    private TextView mBalanceTv;
    private TextView mAddressTv;

    private View mNoTransactionView;

    private View mSendOutBtn;
    private View mReceiptBtn;

    public final static String SHARE_ADDRESS_EXTRA = "share_address_extra";

    private EthAccountDetailHelper mEthAccountDetailHelper;
    private Boolean mIsTokenAccount = false;

    private BtcAccountDetailHelper mBtcAccountDetailHelper;

    private LoaderManager mLoaderManager;
    private AccountBalanceLoaderHelper mAccountBalanceLoaderHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_detail_activity);

        if (getIntent().hasExtra(AppUtils.ACCOUNT_DATA)) {
            mAccountItem = (SerializableAccountItem) getIntent().getSerializableExtra(AppUtils.ACCOUNT_DATA);
        }
        if (getIntent().hasExtra(AppUtils.TOKEN_DATA)) {
            mTokenItem = (RawAccountItem) getIntent().getSerializableExtra(AppUtils.TOKEN_DATA);
            mIsTokenAccount = true;
        }
        setTitle(mAccountItem.getAccountName());
        initViews();
    }

    private void initViews(){
        initHeadView();
        initCenterView();
        initBottomView();
    }

    private void initHeadView(){
        mBalanceTranslateTv = findViewById(R.id.balance_translate_tv);
        mBalanceTv = findViewById(R.id.balance_tv);
        mAddressTv = findViewById(R.id.address_tv);
        mAddressTv.setText(getResources().getString(R.string.address, mAccountItem.getAddress()));
        mAddressTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", mAccountItem.getAddress());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(AccountDetailActivity.this, R.string.has_copied_address, Toast.LENGTH_SHORT).show();
            }
        });

        mLoaderManager = getLoaderManager();
        if (mTokenItem != null) {
            mAccountBalanceLoaderHelper = new AccountBalanceLoaderHelper(this, getLoaderManager(),
                    mAccountItem.getAddress(), mTokenItem.getContractAddress(),
                    new AccountBalanceLoaderHelper.OnDataLoadFinishedListener() {
                        @Override
                        public void onBalanceLoadFinished(String balance) {
                            updateBalance(balance);
                        }
                    });
        } else {
            mAccountBalanceLoaderHelper = new AccountBalanceLoaderHelper(this, getLoaderManager(),
                    mAccountItem.getAddress(),
                    new AccountBalanceLoaderHelper.OnDataLoadFinishedListener() {
                        @Override
                        public void onBalanceLoadFinished(String balance) {
                            updateBalance(balance);
                        }
                    });
        }
    }

    private void initCenterView(){
        mNoTransactionView = findViewById(R.id.no_transaction_view);
        if(mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_BTC){
            initBtcAccount();
        } else {
            initEthAccount();
        }
    }

    private void initBottomView(){
        mSendOutBtn = findViewById(R.id.send_btn);
        mReceiptBtn = findViewById(R.id.receipt_btn);
        mReceiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action_SHARE_ADDRESS_QR_ACTION");
                intent.putExtra(SHARE_ADDRESS_EXTRA,mAccountItem.getAddress());
                intent.putExtra(AppUtils.COIN_TYPE, mTokenItem != null ? LibUtils.COINTYPE.COIN_ETH : mAccountItem.getCoinType());
                startActivity(intent);
            }
        });

        mSendOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action_TRANSFER_TO_ADDRESS_ACTION");
                //intent.putExtra(SHARE_ADDRESS_EXTRA, mAccountItem.getAddress());

                intent.putExtra(AppUtils.ACCOUNT_DATA, mAccountItem);
                if (mIsTokenAccount){
                    //intent.putExtra(TOKEN_EXTRA, mTokenItem);
                    intent.putExtra(AppUtils.TOKEN_DATA, mTokenItem);
                }
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoaderManager != null){
            mLoaderManager.destroyLoader(EthAccountDetailHelper.TX_LIST_LOADER);
            mLoaderManager.destroyLoader(AccountBalanceLoaderHelper.ACCOUNT_BALANCE_LOADER);
            mLoaderManager = null;
        }
        if(mBtcAccountDetailHelper != null){
            mBtcAccountDetailHelper.destory();
        }
    }

    private void updateVisibility(int count){
        mNoTransactionView.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
    }

    private void updateBalance(String balance){
        if (mIsTokenAccount){
            mBalanceTv.setText(TokenUtils.getBalanceText(balance, mTokenItem.getDecimals()) + " " + mTokenItem.getCoinName());
            mBalanceTranslateTv.setText(this.getString(R.string.item_balance, UsdToCnyHelper.getChooseCurrencyUnit(),
                    TokenUtils.getTokenConversionText(balance, mTokenItem.getDecimals(), mTokenItem.getRate())));
        }else {
            if(mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_BTC){
                mBalanceTv.setText(TokenUtils.getBalanceText(balance, BtcUtils.BTC_DECIMALS_COUNT) + " " + mAccountItem.getCoinName());
                mBalanceTranslateTv.setText(getResources().getString(R.string.item_balance, UsdToCnyHelper.getChooseCurrencyUnit(),
                        BtcUtils.getBalanceConversionTextFromRawBalance(balance)));
            } else {
                mBalanceTv.setText(TokenUtils.getBalanceText(balance, TokenUtils.ETH_DECIMALS) + " ETH");
                if(mAccountItem != null){
                    mBalanceTranslateTv.setText(getResources().getString(R.string.item_balance, UsdToCnyHelper.getChooseCurrencyUnit(),
                            TokenUtils.getBalanceConversionText(balance, TokenUtils.ETH_DECIMALS)));
                }
            }
        }
    }

    private void initEthAccount(){
        mEthAccountDetailHelper = new EthAccountDetailHelper();
        mEthAccountDetailHelper.initEthAccount(this, getLoaderManager(), mAccountItem.getAddress(),
                mTokenItem != null,
                mTokenItem != null ? mTokenItem.getContractAddress() : null,
                new EthAccountDetailHelper.OnDataLoadFinishedListener() {
                    @Override
                    public void onDataLoadFinished(int count) {
                        updateVisibility(count);
                    }
                });
    }

    private void initBtcAccount(){
        RecyclerView recyclerView = (RecyclerView)AppUtils.getStubView(this, R.id.transaction_list_btc_view_stub, R.id.listview);
        recyclerView.setVisibility(View.VISIBLE);
        BtcAccountDetailHelper.OnDataLoadFinishedListener onDataLoadFinishedListener = new BtcAccountDetailHelper.OnDataLoadFinishedListener() {
            @Override
            public void onTransationListLoadFinished(int count) {
                updateVisibility(count);
            }
        };
        mBtcAccountDetailHelper = new BtcAccountDetailHelper(this);
        mBtcAccountDetailHelper.init(mAccountItem.getAddress(),
                recyclerView,
                getLoaderManager(),
                onDataLoadFinishedListener
        );
    }
}
