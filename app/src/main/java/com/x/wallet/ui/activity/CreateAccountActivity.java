package com.x.wallet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.x.wallet.AppUtils;
import com.x.wallet.R;


/**
 * Created by wuliang on 18-3-13.
 */

public class CreateAccountActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle();

        setContentView(R.layout.create_account_activity);
    }

    private void setTitle(){
        Intent intent = getIntent();
        int index = intent.getIntExtra("coin_index", 0);
        this.setTitle(this.getString(R.string.create_account_title, AppUtils.COIN_ARRAY[index]));
    }
}
