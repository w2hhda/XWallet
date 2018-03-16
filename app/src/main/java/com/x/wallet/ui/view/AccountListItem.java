package com.x.wallet.ui.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.ui.data.AccountItem;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountListItem extends RelativeLayout{
    private AccountItem mAccountItem;
    private TextView mAccountNameTv;
    private TextView mCoinNameTv;
    private TextView mBalanceTv;

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
        mCoinNameTv = findViewById(R.id.coin_name_tv);
        mBalanceTv = findViewById(R.id.coin_balance_tv);
    }

    public void bind(Cursor cursor) {
        mAccountItem = AccountItem.createFromCursor(cursor);
        mAccountNameTv.setText(mAccountItem.getAccountName());
        Log.i("test4", "AccountListItem bind = " + mAccountItem.getAccountName());
        mCoinNameTv.setText(mAccountItem.getCoinName());
        mBalanceTv.setText(String.valueOf(mAccountItem.getBalance()));
    }
}
