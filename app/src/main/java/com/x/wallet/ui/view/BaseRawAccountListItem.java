package com.x.wallet.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.lib.eth.EthUtils;
import com.x.wallet.transaction.balance.BalanceConversionUtils;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.ui.data.AccountItem;
import com.x.wallet.ui.data.RawAccountItem;

/**
 * Created by wuliang on 18-3-30.
 */

public class BaseRawAccountListItem extends RelativeLayout {
    private AccountItem mAccountItem;

    private ImageView mImageView;
    private TextView mCoinNameTv;
    private TextView mCoinBalanceUnitTv;
    protected TextView mBalanceTv;
    protected TextView mBalanceConversionTv;

    private BalanceConversionUtils.RateUpdateListener mRateUpdateListener;

    public BaseRawAccountListItem(Context context) {
        super(context);
    }

    public BaseRawAccountListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = findViewById(R.id.coin_icon_iv);
        mCoinNameTv = findViewById(R.id.coin_name_tv);
        mCoinBalanceUnitTv = findViewById(R.id.coin_balance_unit_tv);
        mBalanceTv = findViewById(R.id.coin_balance_tv);
        mBalanceConversionTv = findViewById(R.id.coin_balance_conversion_tv);
    }

    public void bind(AccountItem accountItem) {
        if (mRateUpdateListener != null) {
            BalanceConversionUtils.unRegisterListener(mRateUpdateListener);
        }

        mAccountItem = accountItem;
        mCoinNameTv.setText(mAccountItem.getCoinName());
        if (mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_ETH) {
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

    public void updateBalanceConversionText() {
        if (mAccountItem != null) {
            mBalanceConversionTv.setText(getContext().getString(R.string.item_balance, BalanceConversionUtils.calculateBalanceText(mAccountItem.getBalance())));
        }
    }

    public void bind(RawAccountItem accountItem) {
        mImageView.setImageResource(R.drawable.coin_eos_icon);
        mCoinNameTv.setText(accountItem.getCoinName());
        mCoinBalanceUnitTv.setText(accountItem.getCoinName());
        mBalanceTv.setText(TokenUtils.translateToken(accountItem.getBalance(), accountItem.getDecimals()).stripTrailingZeros().toPlainString());
        mBalanceConversionTv.setText(getContext().getString(R.string.item_balance, String.valueOf(TokenUtils.calculateTokenBalance(accountItem.getBalance(), accountItem.getDecimals(), accountItem.getRate()) * BalanceConversionUtils.mUsdToCny)));
    }


    public void initLayout() {
        LayoutInflater.from(getContext()).inflate(R.layout.base_raw_account_list_item, this, true);
    }

    public AccountItem getAccountItem() {
        return mAccountItem;
    }
}
