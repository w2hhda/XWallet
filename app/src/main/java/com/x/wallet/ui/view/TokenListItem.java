package com.x.wallet.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.x.wallet.R;
import com.x.wallet.ui.data.TokenItemBean;

/**
 * Created by wuliang on 18-3-30.
 */

public class TokenListItem extends RelativeLayout{
    private ImageView mImageView;
    private TextView mShortNameTv;
    private TextView mWholeNameTv;
    private ImageView mCheckIv;

    private TokenItemBean mTokenItem;

    public static final String BASE_URL = "https://raw.githubusercontent.com/TrustWallet/tokens/master/images/";

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
        Picasso.get().load(BASE_URL + mTokenItem.getContractAddress().toLowerCase() + ".png").into(mImageView);
    }

    public TokenItemBean getTokenItem() {
        return mTokenItem;
    }
}
