package com.x.wallet.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by wuliang on 18-3-14.
 */

public class BaseAppCompatActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            if (getSupportActionBar() != null) {
                getSupportActionBar().setElevation(0);
            }
        }
    }
}
