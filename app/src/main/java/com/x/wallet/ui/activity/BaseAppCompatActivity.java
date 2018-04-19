package com.x.wallet.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.x.wallet.AppUtils;

import org.web3j.abi.datatypes.Int;

/**
 * Created by wuliang on 18-3-14.
 */

public class BaseAppCompatActivity extends AppCompatActivity{
    private static final int CONFIRM_REQUEST_CODE = 319;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            if (getSupportActionBar() != null) {
                getSupportActionBar().setElevation(0);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppUtils.startFromBg() && AppUtils.hasPin()){
            Intent intent = new Intent("com.x.wallet.action.CONFIRM_PIN_ACTION");
            intent.putExtra(AppUtils.CONFIRM_PIN_CODE, AppUtils.getPin());
            startActivityForResult(intent, CONFIRM_REQUEST_CODE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED && CONFIRM_REQUEST_CODE == requestCode){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            finish();
        }
        if (CONFIRM_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK){
            AppUtils.setBackground(false);
        }
    }

}
