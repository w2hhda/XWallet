package com.x.wallet.ui.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.transaction.token.TokenIconUtils;
import com.x.wallet.ui.data.TokenItem;

public class AccountTokenListItem extends RelativeLayout{
    private ImageView mImageView;
    private TextView mShortNameTv;
    private TokenItem mTokenItem;

    public AccountTokenListItem(Context context) {
        super(context);
    }

    public AccountTokenListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccountTokenListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = findViewById(R.id.token_icon_iv);
        mShortNameTv = findViewById(R.id.shortname_tv);
    }

    public void bind(Cursor cursor){
        mTokenItem = TokenItem.createFromCursor(cursor);
        mShortNameTv.setText(mTokenItem.getSymbol());
        TokenIconUtils.setImage(mImageView, mTokenItem.getContractAddress());
    }

    public TokenItem getTokenItem() {
        return mTokenItem;
    }
}
