package com.x.wallet.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;
import com.x.wallet.ui.data.AllAccountItem;

/**
 * Created by wuliang on 18-3-30.
 */

public class RawAccountListItem extends RelativeLayout {
    private AllAccountItem mAccountItem;

    private ImageView mImageView;
    private TextView mCoinNameTv;
    private TextView mCoinBalanceUnitTv;
    protected TextView mBalanceTv;
    protected TextView mBalanceConversionTv;

    public RawAccountListItem(Context context) {
        super(context);
    }

    public RawAccountListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.raw_account_list_item, this, true);
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

    public void bind(AllAccountItem accountItem) {
        mAccountItem = accountItem;

        mCoinNameTv.setText(mAccountItem.getCoinName());
        if (mAccountItem.getAllCoinType() == AllAccountItem.COIN_TYPE_MAIN) {
            if (mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_ETH) {
                mImageView.setImageResource(R.drawable.eth);
                mCoinBalanceUnitTv.setText(R.string.coin_unit_eth);
                mBalanceTv.setText(TokenUtils.getBalanceText(mAccountItem.getBalance(), TokenUtils.ETH_DECIMALS));
                mBalanceConversionTv.setText(getContext().getString(R.string.item_balance, UsdToCnyHelper.getChooseCurrencyUnit(),
                        TokenUtils.getBalanceConversionText(mAccountItem.getBalance(), TokenUtils.ETH_DECIMALS)));
            }
        } else {
            mCoinBalanceUnitTv.setText(accountItem.getCoinName());
            mBalanceTv.setText(TokenUtils.getBalanceText(mAccountItem.getBalance(), accountItem.getDecimals()));
            mBalanceConversionTv.setText(getContext().getString(R.string.item_balance, UsdToCnyHelper.getChooseCurrencyUnit(),
                    TokenUtils.getTokenConversionText(accountItem.getBalance(), accountItem.getDecimals(), accountItem.getRate())));
            Picasso.get().load(TokenListItem.BASE_URL + mAccountItem.getContractAddress() + ".png").into(mImageView);
            Log.i(AppUtils.APP_TAG, "RawAccountListItem bind balance = " + accountItem.getBalance() + ", rate = " + accountItem.getRate());
        }
    }
    public AllAccountItem getAccountItem() {
        return mAccountItem;
    }
}
