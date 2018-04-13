package com.x.wallet.ui.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.ui.data.TokenItem;

/**
 * Created by wuliang on 18-3-30.
 */

public class ManagerTokenListItem extends RelativeLayout{
    private ImageView mImageView;
    private TextView mShortNameTv;
    private ImageView mCheckIv;

    private TokenItem mTokenItem;

    public static final String BASE_URL = "file:///android_asset/";

    public ManagerTokenListItem(Context context) {
        super(context);
    }

    public ManagerTokenListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ManagerTokenListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = findViewById(R.id.token_icon);
        mShortNameTv = findViewById(R.id.shortname_tv);
        mCheckIv = findViewById(R.id.check_iv);
    }

    public void bind(TokenItem tokenItem){
        mTokenItem = tokenItem;
        mShortNameTv.setText(tokenItem.getSymbol());
        AppUtils.setImage(mImageView, mTokenItem.getContractAddress());
    }

    public void bind(Cursor cursor){
        mTokenItem = TokenItem.createFromCursor(cursor);
        mShortNameTv.setText(mTokenItem.getSymbol());
        AppUtils.setImage(mImageView, mTokenItem.getContractAddress());
    }

    public TokenItem getTokenItem() {
        return mTokenItem;
    }
}
