package com.x.wallet.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.x.wallet.R;
import com.x.wallet.transaction.address.CreateAddressAsycTask;
import com.x.wallet.ui.activity.CreateAccountActivity;

import net.bither.bitherj.crypto.SecureCharSequence;

/**
 * Created by wuliang on 18-3-16.
 */

public class SetPasswordView extends LinearLayout{
    private EditText mPasswordEt;
    private EditText mConfirmPasswordEt;

    public SetPasswordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.set_password_view, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPasswordEt = findViewById(R.id.set_password_et);
        mConfirmPasswordEt = findViewById(R.id.confirm_password_et);
    }

    public String getPassword(){
        return mPasswordEt.getText() != null ? mPasswordEt.getText().toString() : null;
    }

    private int isPasswordOk(){
        if(TextUtils.isEmpty(mPasswordEt.getText())){
            return PasswordErrorType.BLANK;
        }

        SecureCharSequence password = null;
        SecureCharSequence passwordConfirm = null;
        try{
            password = new SecureCharSequence(mPasswordEt.getText());
            passwordConfirm = new SecureCharSequence(mConfirmPasswordEt.getText());

            if(password.length() < 6){
                return PasswordErrorType.SHORT;
            }

            if(!password.equals(passwordConfirm)){
                return PasswordErrorType.NOT_THE_SAME;
            }
        } finally {
            if(password != null){
                password.wipe();
            }
            if(passwordConfirm != null){
                passwordConfirm.wipe();
            }
        }

        return PasswordErrorType.OK;
    }

    public boolean checkInputPassword(Context context){
        int passwordCheckResult = isPasswordOk();
        switch (passwordCheckResult){
            case SetPasswordView.PasswordErrorType.BLANK:
                Toast.makeText(context, R.string.password_error_blank, Toast.LENGTH_LONG).show();
                return false;
            case SetPasswordView.PasswordErrorType.NOT_THE_SAME:
                Toast.makeText(context, R.string.password_error_not_the_same, Toast.LENGTH_LONG).show();
                return false;
            case SetPasswordView.PasswordErrorType.SHORT:
                Toast.makeText(context, R.string.password_error_short, Toast.LENGTH_LONG).show();
                return false;
            case SetPasswordView.PasswordErrorType.OK:
                return true;
        }
        return false;
    }

    public interface PasswordErrorType{
        int OK = 0;
        int SHORT = 1;
        int NOT_THE_SAME = 2;
        int BLANK = 3;
        int ERROR_UNKNOWN = 4;
    }
}
