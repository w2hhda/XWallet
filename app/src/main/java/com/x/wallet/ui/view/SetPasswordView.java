package com.x.wallet.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.x.wallet.R;

/**
 * Created by wuliang on 18-3-16.
 */

public class SetPasswordView extends LinearLayout{
    private EditText mPasswordEt;

    public SetPasswordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.set_password_view, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPasswordEt = findViewById(R.id.set_password_et);
    }

    public String getPassword(){
        return mPasswordEt.getText() != null ? mPasswordEt.getText().toString() : null;
    }
}
