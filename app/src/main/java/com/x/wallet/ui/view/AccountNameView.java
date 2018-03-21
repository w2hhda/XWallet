package com.x.wallet.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.x.wallet.R;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountNameView extends LinearLayout {
    private EditText mAccountNameEt;
    private static final int ACCOUNTNAME_MAX_LENGTH = 20;

    public AccountNameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.account_name_view, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAccountNameEt = findViewById(R.id.account_name_et);
    }

    public String getAccountName(){
        return mAccountNameEt.getText() != null ? mAccountNameEt.getText().toString().trim() : "";
    }

    public boolean isAccountNameOk(){
        if(TextUtils.isEmpty(mAccountNameEt.getText())) return false;
        int length = mAccountNameEt.getText().toString().trim().length();
        return length < ACCOUNTNAME_MAX_LENGTH && length > 0;
    }
}
