package com.x.wallet.ui.fingerprint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;

public class ConfirmPinActivity extends Activity implements PwdView.InputCallBack {    private PwdView psw_input;
    private TextView inputHint;
    private String firstPinCode;
    private String confirmPinCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_password);
        initView();
    }

    private void initView(){
        if (getIntent() != null){
            firstPinCode = getIntent().getStringExtra(AppUtils.PIN_TAG);
            confirmPinCode = getIntent().getStringExtra(AppUtils.CONFIRM_PIN_CODE);
        }

        inputHint = findViewById(R.id.please_input_pw);
        psw_input = findViewById(R.id.pwdView);
        InputMethodView inputMethodView = findViewById(R.id.inputMethodView);
        psw_input.setInputMethodView(inputMethodView);
        psw_input.setInputCallBack(this);
        inputHint.setText(getResources().getString(R.string.confirm_pin_code));
    }

    @Override
    public void onInputFinish(String result) {
        if (!TextUtils.isEmpty(firstPinCode) && !TextUtils.isEmpty(result) && result.equals(firstPinCode)){
            AppUtils.log("repeat pw correct.");
            Intent intent = new Intent();
            intent.putExtra(AppUtils.PIN_TAG, result);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }else if(!TextUtils.isEmpty(confirmPinCode) && !TextUtils.isEmpty(result) && AppUtils.getStringMD5(result).equals(confirmPinCode)){
            AppUtils.log("confirm to reset checkbox");
            setResult(Activity.RESULT_OK);
            finish();
        }else {
            inputHint.setText(getResources().getString(R.string.wrong_pin_code));
            inputHint.setTextColor(getResources().getColor(R.color.colorRed));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        setResult(Activity.RESULT_CANCELED);
    }
}
