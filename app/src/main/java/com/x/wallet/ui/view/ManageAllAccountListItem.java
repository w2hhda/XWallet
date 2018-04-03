package com.x.wallet.ui.view;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.transaction.balance.BalanceConversionUtils;
import com.x.wallet.ui.data.AccountItem;

/**
 * Created by wuliang on 18-3-28.
 */

public class ManageAllAccountListItem extends CardView {
    private AccountItem mAccountItem;

    private TextView mAccountNameTv;
    private View mNotBackupTv;
    private TextView mAddressTv;
    private TextView mBalanceTv;
    private TextView mCoinBalanceUnitTv;

    public ManageAllAccountListItem(Context context) {
        super(context);
    }

    public ManageAllAccountListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ManageAllAccountListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAccountNameTv = findViewById(R.id.account_name_tv);
        mNotBackupTv = findViewById(R.id.not_backup_tv);
        mAddressTv = findViewById(R.id.address_tv);
        mBalanceTv = findViewById(R.id.balance_tv);
        mCoinBalanceUnitTv = findViewById(R.id.coin_balance_unit_tv);
    }

    public void bind(Cursor cursor) {
        mAccountItem = AccountItem.createFromCursor(cursor);
        mAccountNameTv.setText(mAccountItem.getAccountName());
        mAddressTv.setText(mAccountItem.getAddress());
        mBalanceTv.setText(BalanceConversionUtils.getEthBalanceText(mAccountItem.getBalance()));
        mCoinBalanceUnitTv.setText(R.string.coin_unit_eth);
    }

    public AccountItem getAccountItem() {
        return mAccountItem;
    }
}
