package com.x.wallet.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.x.wallet.R;

/**
 * Created by wuliang on 18-5-3.
 */

public class AddToUsualUseAddressView extends LinearLayout{
    public AddToUsualUseAddressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.add_to_usual_use_address_view, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

}
