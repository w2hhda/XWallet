package com.x.wallet.ui.view;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.XWalletApplication;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.balance.BalanceLoaderManager;
import com.x.wallet.transaction.balance.ItemLoadedCallback;
import com.x.wallet.ui.data.AccountItem;
import com.x.wallet.ui.data.RawAccountItem;
import com.x.wallet.ui.data.TokenItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountListItem extends LinearLayout {
    private AccountItem mAccountItem;
    private TextView mAccountNameTv;
    private BaseRawAccountListItem mRawAccountListItem;
    private View mAddTokenView;
    private LinearLayout mTokenContainer;

    public AccountListItem(Context context) {
        super(context);
    }

    public AccountListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccountListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAccountNameTv = findViewById(R.id.account_name_tv);

        mAddTokenView = findViewById(R.id.add_token_container);
        mAddTokenView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action.ADD_TOKEN_ACTION");
                intent.putExtra(AppUtils.ACCOUNT_ID, mAccountItem.getId());
                intent.putExtra(AppUtils.ACCOUNT_ADDRESS, mAccountItem.getAddress());
                intent.putExtra(AppUtils.HAS_TOKEN_KEY, mAccountItem.isHasToken());
                getContext().startActivity(intent);
            }
        });

        mRawAccountListItem = findViewById(R.id.raw_item);

        mTokenContainer = findViewById(R.id.token_container);
    }

    public void bind(Cursor cursor) {
        mAccountItem = AccountItem.createFromCursor(cursor);
        Log.i(AppUtils.APP_TAG, "AccountListItem bind mAccountItem = " + mAccountItem);

        mAccountNameTv.setText(mAccountItem.getAccountName());
        if (mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_ETH) {
            mAddTokenView.setVisibility(VISIBLE);
        } else {
            mAddTokenView.setVisibility(GONE);
        }

        mRawAccountListItem.bind(mAccountItem);

        bindTokenList();
    }

    private void bindTokenList() {
        mTokenContainer.removeAllViews();
        if (mAccountItem.isHasToken()) {
            List<TokenItem> list = mAccountItem.getTokenItemList();
            if (list != null && list.size() > 0) {
                bindToken();
            } else {
                mAccountItem.setTokenLoadedCallback(new AccountItem.TokenLoadedCallback() {
                    @Override
                    public void onTokenLoaded(AccountItem accountItem) {
                        if (accountItem != null && mAccountItem != null &&
                                accountItem.getId() == mAccountItem.getId()) {
                            bindToken();
                            queryTokenBalance();
                        }
                    }
                });
            }
        }
    }

    private void bindToken() {
        List<TokenItem> list = mAccountItem.getTokenItemList();
        if (list != null && list.size() > 0) {
            for (TokenItem item : list) {
                RawAccountListItem listItem = (RawAccountListItem) LayoutInflater.from(getContext()).inflate(
                        R.layout.raw_account_list_item, mTokenContainer, false);
                RawAccountItem accountItem = new RawAccountItem(item.getSymbol(), item.getIdInAll(), item.getBalance(), item.getDecimals(), item.getRate());
                listItem.bind(accountItem);
                mTokenContainer.addView(LayoutInflater.from(getContext()).inflate(
                        R.layout.token_list_item_divider_view, mTokenContainer, false));
                mTokenContainer.addView(listItem);
            }
        }
    }

    private void bindTokenBalance(ArrayList<String> balance, ArrayList<Double> rateList) {
        int count = mTokenContainer.getChildCount();
        Log.i(AppUtils.APP_TAG, "AccountListItem bindTokenBalance count = " + count);
        for(int i = 0; i < count; i++){
            View view = mTokenContainer.getChildAt(i);
            Log.i(AppUtils.APP_TAG, "AccountListItem bindTokenBalance view = " + view);
            if(view != null && view instanceof  RawAccountListItem){
                RawAccountListItem listItem = (RawAccountListItem) view;
                Log.i(AppUtils.APP_TAG, "AccountListItem bindTokenBalance balance.get(i) = " + balance.get(0));
                Log.i(AppUtils.APP_TAG, "AccountListItem bindTokenBalance rateList.get(i) = " + rateList.get(0));
                listItem.bindBalance(balance.get(0), rateList.get(0));
            }
        }
    }

    public AccountItem getAccountItem() {
        return mAccountItem;
    }

    private void queryTokenBalance(){
        XWalletApplication.getApplication().getBalanceLoaderManager().getBalance(Uri.parse("content://com.x.wallet/token/" + mAccountItem.getAddress()), new ItemLoadedCallback<BalanceLoaderManager.BalanceLoaded>() {
            @Override
            public void onItemLoaded(BalanceLoaderManager.BalanceLoaded result, Throwable exception) {
                Log.i(AppUtils.APP_TAG, "AccountListItem result.mAddress = " + result.mAddress + ", mAccountItem.getAddress() = " + mAccountItem.getAddress());
                bindTokenBalance(result.mBalanceList, result.mRateList);
            }
        });
    }
}
