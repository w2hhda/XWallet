package com.x.wallet.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.x.wallet.R;

/**
 * Created by wuliang on 18-3-28.
 */

public class PrivacyPolicyView extends LinearLayout{
    private CheckBox mDeclareCheckBox;

    public PrivacyPolicyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.privacy_policy_view, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDeclareCheckBox = findViewById(R.id.checkbox);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        mDeclareCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);
    }
}
