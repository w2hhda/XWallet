package com.x.wallet.ui.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.ui.data.AllAccountItem;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountListItem extends LinearLayout {
    private AllAccountItem mAccountItem;
    private TextView mAccountNameTv;
    private RawAccountListItem mRawAccountListItem;
    private View mDividerAboveHeader;
    private View mDividerAboveItem;

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

        mRawAccountListItem = findViewById(R.id.raw_item);

        mDividerAboveHeader = findViewById(R.id.divider_above_header);
        mDividerAboveItem = findViewById(R.id.divider_above_item);
    }

    public void bind(Cursor cursor) {
        mAccountItem = AllAccountItem.createFromCursor(cursor);
        Log.i(AppUtils.APP_TAG, "AccountListItem bind mAccountItem = " + mAccountItem);

        if (mAccountItem.isToken()) {
            mDividerAboveHeader.setVisibility(GONE);
            mAccountNameTv.setVisibility(GONE);
            mDividerAboveItem.setVisibility(VISIBLE);
        } else {
            if(cursor.getPosition() == 0){
                mDividerAboveHeader.setVisibility(GONE);
            } else {
                mDividerAboveHeader.setVisibility(VISIBLE);
            }
            mAccountNameTv.setVisibility(VISIBLE);
            mAccountNameTv.setText(mAccountItem.getAccountName());
            mDividerAboveItem.setVisibility(GONE);
        }
        mRawAccountListItem.bind(mAccountItem);
    }
}
