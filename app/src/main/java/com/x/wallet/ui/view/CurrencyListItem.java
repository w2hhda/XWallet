package com.x.wallet.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.R;

/**
 * Created by admin on 2018/4/7.
 */

public class CurrencyListItem extends RelativeLayout{
    private TextView mCurrencyTv;
    private ImageView mCheckIv;

    public CurrencyListItem(Context context) {
        super(context);
    }

    public CurrencyListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CurrencyListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCurrencyTv = findViewById(R.id.currency_tv);
        mCheckIv = findViewById(R.id.check_iv);
    }

    public void bind(String currency, String chooseCurrency){
        mCurrencyTv.setText(currency);
        mCheckIv.setImageResource(currency.equals(chooseCurrency) ? R.drawable.ic_radio_button_checked : R.drawable.ic_radio_button_unchecked);
    }
}
