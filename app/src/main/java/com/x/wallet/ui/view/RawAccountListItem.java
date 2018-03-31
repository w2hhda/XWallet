package com.x.wallet.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import com.x.wallet.R;
import com.x.wallet.transaction.balance.BalanceConversionUtils;
import com.x.wallet.transaction.token.TokenUtils;

/**
 * Created by wuliang on 18-3-30.
 */

public class RawAccountListItem extends BaseRawAccountListItem{

    public RawAccountListItem(Context context) {
        super(context);
    }

    public RawAccountListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initLayout() {
        //do nothing
    }

    public void bindBalance(String balance, double rate) {
        mBalanceTv.setText(balance);
        mBalanceConversionTv.setText(getContext().getString(R.string.item_balance, String.valueOf(Double.valueOf(balance) * rate * BalanceConversionUtils.mUsdToCny)));

    }
}
