package com.x.wallet.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.ui.data.TokenItem;

/**
 * Created by wuliang on 18-3-30.
 */

public class TokenListItem extends RelativeLayout{
    private ImageView mImageView;
    private TextView mShortNameTv;
    private TextView mWholeNameTv;
    private RadioButton mRadioButton;

    private TokenItem mTokenItem;

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
        mImageView = findViewById(R.id.iv);
        mShortNameTv = findViewById(R.id.shortname_tv);
        mWholeNameTv = findViewById(R.id.wholename_tv);
        mRadioButton = findViewById(R.id.radioBtn);
    }

    public void bind(TokenItem tokenItem, boolean isChecked){
        mTokenItem = tokenItem;
        mShortNameTv.setText(tokenItem.getShortname());
        mWholeNameTv.setText(tokenItem.getWholename());
        mRadioButton.setChecked(isChecked);
    }

    public boolean isSelected(){
        return mRadioButton.isChecked();
    }

    public TokenItem getTokenItem() {
        return mTokenItem;
    }
}
