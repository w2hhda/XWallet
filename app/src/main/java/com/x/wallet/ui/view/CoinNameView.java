package com.x.wallet.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.x.wallet.R;

/**
 * Created by wuliang on 18-3-16.
 */

public class CoinNameView extends FrameLayout {
    private TextView mCoinNameTv;

    public CoinNameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.coin_name_view, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCoinNameTv = findViewById(R.id.coin_name_tv);
    }

    public void setCoinName(String coinName) {
        mCoinNameTv.setText(coinName);
    }
}
