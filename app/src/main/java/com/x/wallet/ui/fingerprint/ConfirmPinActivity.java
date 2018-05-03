package com.x.wallet.ui.fingerprint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.XWalletApplication;
import com.x.wallet.ui.fragment.SettingsFragment;

public class ConfirmPinActivity extends Activity implements PwdView.InputCallBack , FingerPrintAuthCallback{
    private PwdView psw_input;
    private TextView inputHint;
    private String firstPinCode;
    private String confirmPinCode;
    private boolean resetPinCode;
    private ImageView mFingerprintIv;
    private AlertDialog dialog;
    private FingerPrintAuthHelper fingerPrintAuthHelper;

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
            resetPinCode = getIntent().getBooleanExtra(SettingsFragment.TO_CONFONFIRM_PIN, false);
        }

        inputHint = findViewById(R.id.please_input_pw);
        psw_input = findViewById(R.id.pwdView);
        InputMethodView inputMethodView = findViewById(R.id.inputMethodView);
        psw_input.setInputMethodView(inputMethodView);
        psw_input.setInputCallBack(this);
        inputHint.setText(getResources().getString(R.string.confirm_pin_code));
        mFingerprintIv = findViewById(R.id.fingerprint_iv);
        if (!TextUtils.isEmpty(confirmPinCode) && !resetPinCode) {
            mFingerprintIv.setVisibility(View.VISIBLE);
            fingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);
        }
//        mFingerprintIv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                createFingerprintDialog();
//            }
//        });
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
        if (fingerPrintAuthHelper != null){
            fingerPrintAuthHelper.stopAuth();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(confirmPinCode) && !resetPinCode){
            //createFingerprintDialog();
            fingerPrintAuthHelper.startAuth();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void createFingerprintDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialog = builder.setIcon(R.drawable.ic_fingerprint)
                .setTitle(R.string.use_fingerprint)
                .setCancelable(false)
                .setPositiveButton(getText(R.string.use_pin), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fingerPrintAuthHelper.stopAuth();
                    }
                }).create();
        dialog.show();
        fingerPrintAuthHelper.startAuth();
    }

    @Override
    public void onNoFingerPrintHardwareFound() {
        noFingerprint();
    }

    @Override
    public void onNoFingerPrintRegistered() {
        noFingerprint();
    }

    @Override
    public void onBelowMarshmallow() {
        dismissFingerprintAuto(true);
    }

    @Override
    public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
        authSuccess();
    }

    @Override
    public void onAuthFailed(int errorCode, String errorMessage) {
        switch (errorCode) {
            case AuthErrorCodes.CANNOT_RECOGNIZE_ERROR:
                inputHint.setText(getResources().getString(R.string.cannot_recognize));
                //dialog.setTitle(getText(R.string.cannot_recognize));
                break;
            case AuthErrorCodes.NON_RECOVERABLE_ERROR:
                dismissFingerprintAuto(true);
                break;
            case AuthErrorCodes.RECOVERABLE_ERROR:
                inputHint.setText(errorMessage);
                break;
            case AuthErrorCodes.CANCELLED_BY_USED:
                dismissFingerprintAuto(false);
                break;
        }
    }

    private void authSuccess(){
        setResult(Activity.RESULT_OK);
        if (fingerPrintAuthHelper != null){
            fingerPrintAuthHelper.stopAuth();
        }
        finish();
    }

    private void dismissFingerprintAuto(boolean unrecoverableError){
        if (dialog != null){
            dialog.dismiss();
        }
        fingerPrintAuthHelper.stopAuth();
        if (unrecoverableError) {
            mFingerprintIv.setVisibility(View.INVISIBLE);
            inputHint.setText(getResources().getString(R.string.cannot_use_fingerprint));
        }else {
            inputHint.setText(getResources().getString(R.string.confirm_pin_code));
        }
    }

    private void noFingerprint(){
        mFingerprintIv.setVisibility(View.GONE);
        inputHint.setText(getResources().getString(R.string.confirm_pin_code));
    }
}
