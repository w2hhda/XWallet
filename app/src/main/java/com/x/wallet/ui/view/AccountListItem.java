package com.x.wallet.ui.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.lib.eth.EthUtils;
import com.x.wallet.transaction.balance.BalanceConversionUtils;
import com.x.wallet.ui.data.AccountItem;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountListItem extends RelativeLayout{
    private AccountItem mAccountItem;
    private TextView mAccountNameTv;
    private ImageView mImageView;
    private TextView mCoinNameTv;
    private TextView mCoinBalanceUnitTv;
    private TextView mBalanceTv;
    private TextView mBalanceConversionTv;

    private BalanceConversionUtils.RateUpdateListener mRateUpdateListener;

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
        mImageView = findViewById(R.id.coin_icon_iv);
        mCoinNameTv = findViewById(R.id.coin_name_tv);
        mCoinBalanceUnitTv = findViewById(R.id.coin_balance_unit_tv);
        mBalanceTv = findViewById(R.id.coin_balance_tv);
        mBalanceConversionTv = findViewById(R.id.coin_balance_conversion_tv);
    }

    public void bind(Cursor cursor) {
        if(mRateUpdateListener != null){
            BalanceConversionUtils.unRegisterListener(mRateUpdateListener);
        }
        mAccountItem = AccountItem.createFromCursor(cursor);
        mAccountNameTv.setText(mAccountItem.getAccountName());
        mCoinNameTv.setText(mAccountItem.getCoinName());

        Log.i(AppUtils.APP_TAG, "AccountListItem bind mAccountItem = " + mAccountItem);
        if(mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_ETH){
            mImageView.setImageResource(R.drawable.eth);
            mBalanceTv.setText(EthUtils.getBalanceText(mAccountItem.getBalance()));
            mCoinBalanceUnitTv.setText(R.string.coin_unit_eth);
            updateBalanceConversionText();
        }

        mRateUpdateListener = new BalanceConversionUtils.RateUpdateListener() {
            @Override
            public void onRateUpdate() {
                mImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        updateBalanceConversionText();
                    }
                });
            }
        };
        BalanceConversionUtils.registerListener(mRateUpdateListener);
    }

    public void updateBalanceConversionText(){
        if(mAccountItem != null){
            mBalanceConversionTv.setText(getContext().getString(R.string.item_balance, BalanceConversionUtils.calculateBalanceText(mAccountItem.getBalance())));
        }
    }

    public AccountItem getAccountItem() {
        return mAccountItem;
    }
}
