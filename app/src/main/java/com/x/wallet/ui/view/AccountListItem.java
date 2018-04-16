package com.x.wallet.ui.view;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.ui.data.AllAccountItem;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountListItem extends LinearLayout {
    private AllAccountItem mAccountItem;
    private View mAccountNameViewContainer;
    private TextView mAccountNameTv;
    private RawAccountListItem mRawAccountListItem;
//    private View mAddTokenView;
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
        mAccountNameViewContainer = findViewById(R.id.account_name_container);
        mAccountNameTv = findViewById(R.id.account_name_tv);

//        mAddTokenView = findViewById(R.id.add_token_container);
//        mAddTokenView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent("com.x.wallet.action.ADD_TOKEN_ACTION");
//                intent.putExtra(AppUtils.ACCOUNT_ID, mAccountItem.getId());
//                intent.putExtra(AppUtils.ACCOUNT_ADDRESS, mAccountItem.getAddress());
//                intent.putExtra(AppUtils.HAS_TOKEN_KEY, mAccountItem.isHasToken());
//                getContext().startActivity(intent);
//            }
//        });

        mRawAccountListItem = findViewById(R.id.raw_item);

        mDividerAboveHeader = findViewById(R.id.divider_above_header);
        mDividerAboveItem = findViewById(R.id.divider_above_item);
    }

    public void bind(Cursor cursor) {
        mAccountItem = AllAccountItem.createFromCursor(cursor);
        Log.i(AppUtils.APP_TAG, "AccountListItem bind mAccountItem = " + mAccountItem);

        if (mAccountItem.isToken()) {
            mDividerAboveHeader.setVisibility(GONE);
            mAccountNameViewContainer.setVisibility(GONE);
            mDividerAboveItem.setVisibility(VISIBLE);
        } else {
            if(cursor.getPosition() == 0){
                mDividerAboveHeader.setVisibility(GONE);
            } else {
                mDividerAboveHeader.setVisibility(VISIBLE);
            }
            mAccountNameViewContainer.setVisibility(VISIBLE);
            mAccountNameTv.setText(mAccountItem.getAccountName());
//            if (mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_ETH) {
//                mAddTokenView.setVisibility(VISIBLE);
//            } else {
//                mAddTokenView.setVisibility(GONE);
//            }
            mDividerAboveItem.setVisibility(GONE);
        }
        mRawAccountListItem.bind(mAccountItem);
    }
}
