package com.x.wallet.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.x.wallet.AppUtils;
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

    public void bindBalance(double translateBalance, double rate) {
        Log.i(AppUtils.APP_TAG, "RawAccountListItem bindBalance balance = " + translateBalance + ", rate = " + rate);
        mBalanceTv.setText(String.valueOf(translateBalance));
        mBalanceConversionTv.setText(TokenUtils.getTokenConversionText(getContext(), translateBalance, rate));
    }
}
