package com.x.wallet.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.ui.activity.ServicePolicyActivity;

/**
 * Created by wuliang on 18-3-28.
 */

public class PrivacyPolicyView extends LinearLayout{
    private CheckBox mDeclareCheckBox;
    private TextView mServicePolicyTv;
    private TextView mPrivacyPolicyTv;

    public PrivacyPolicyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.privacy_policy_view, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDeclareCheckBox = findViewById(R.id.checkbox);
        mServicePolicyTv = findViewById(R.id.service_policy_tv);
        mPrivacyPolicyTv = findViewById(R.id.privacy_policy_tv);
        mServicePolicyTv.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG);
        mServicePolicyTv.getPaint().setAntiAlias(true);
        mServicePolicyTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action_SEE_SERVICE_POLICYL");
                intent.putExtra(ServicePolicyActivity.TYPE_TAG, ServicePolicyActivity.TYPE_SERVICE);
                getContext().startActivity(intent);
            }
        });
        mPrivacyPolicyTv.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG);
        mPrivacyPolicyTv.getPaint().setAntiAlias(true);
        mPrivacyPolicyTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action_SEE_SERVICE_POLICYL");
                intent.putExtra(ServicePolicyActivity.TYPE_TAG, ServicePolicyActivity.TYPE_PRIVACY);
                getContext().startActivity(intent);
            }
        });
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        mDeclareCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);
    }
}
