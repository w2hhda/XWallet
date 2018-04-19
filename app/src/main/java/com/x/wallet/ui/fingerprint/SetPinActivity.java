package com.x.wallet.ui.fingerprint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;

public class SetPinActivity extends Activity implements PwdView.InputCallBack {
    private PwdView psw_input;
    private TextView inputHint;
    private static final int CONFIRM_PIN_REQUEST = 29;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_password);
        initView();
    }

    private void initView(){
        inputHint = findViewById(R.id.please_input_pw);
        psw_input = findViewById(R.id.pwdView);
        InputMethodView inputMethodView = findViewById(R.id.inputMethodView);
        psw_input.setInputMethodView(inputMethodView);
        psw_input.setInputCallBack(this);

    }

    @Override
    public void onInputFinish(String result) {
        Intent intent = new Intent("com.x.wallet.action.CONFIRM_PIN_ACTION");
        intent.putExtra(AppUtils.PIN_TAG, result);
        startActivityForResult(intent, CONFIRM_PIN_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (CONFIRM_PIN_REQUEST == requestCode && resultCode == Activity.RESULT_OK){
            AppUtils.log("start to set result ");
            AppUtils.setBackground(false);
            setResult(Activity.RESULT_OK, data);
            finish();
        }else {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }
}
