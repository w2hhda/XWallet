package com.x.wallet.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.ui.data.TokenItemBean;

import java.io.IOException;

/**
 * Created by wuliang on 18-3-30.
 */

public class TokenListItem extends RelativeLayout{
    private ImageView mImageView;
    private TextView mShortNameTv;
    private TextView mWholeNameTv;
    private ImageView mCheckIv;

    private TokenItemBean mTokenItem;

    public static final String BASE_URL = "file:///android_asset/";

    public TokenListItem(Context context) {
        super(context);
    }

    public TokenListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TokenListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = findViewById(R.id.token_icon);
        mShortNameTv = findViewById(R.id.shortname_tv);
        mWholeNameTv = findViewById(R.id.wholename_tv);
        mCheckIv = findViewById(R.id.check_iv);
    }

    public void bind(TokenItemBean tokenItem, boolean isChecked){
        mTokenItem = tokenItem;
        mShortNameTv.setText(tokenItem.getSymbol());
        mWholeNameTv.setText(tokenItem.getName());
        mCheckIv.setImageResource(isChecked ? R.drawable.ic_radio_button_checked : R.drawable.ic_radio_button_unchecked);
        if (isChecked){
            this.setClickable(false);
        }
        AppUtils.setImage(mImageView, mTokenItem.getContractAddress());
    }

    public TokenItemBean getTokenItem() {
        return mTokenItem;
    }
}
