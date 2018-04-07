package com.x.wallet.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;

/**
 * Created by admin on 2018/4/7.
 */

public class ChangeCurrencyPrefView extends RelativeLayout{
    private TextView mTv;

    public ChangeCurrencyPrefView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.change_currency_pref, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTv = findViewById(R.id.current_currency_tv);
    }

    public void updateCurrentCurrencyText(){
        mTv.setText(UsdToCnyHelper.getChooseCurrency());
    }

    public void updateCurrentCurrencyText(String currency){
        mTv.setText(currency);
    }
}
