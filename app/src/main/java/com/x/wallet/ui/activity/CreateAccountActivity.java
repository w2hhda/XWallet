package com.x.wallet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.transaction.address.AddressUtils;
import com.x.wallet.transaction.address.CreateAddressAsycTask;


/**
 * Created by wuliang on 18-3-13.
 */

public class CreateAccountActivity extends BaseAppCompatActivity {

    private View mCreateAcountView;
    private int mCoinType;
    private EditText mAccountNameEt;
    private EditText mPasswordEt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle();

        setContentView(R.layout.create_account_activity);

        initView();
    }

    private void setTitle(){
        Intent intent = getIntent();
        int index = intent.getIntExtra("coin_index", 0);
        mCoinType = index;
        this.setTitle(this.getString(R.string.create_account_title, AppUtils.COIN_ARRAY[index]));
    }

    private void initView(){
        mPasswordEt = findViewById(R.id.set_password_et);
        mAccountNameEt = findViewById(R.id.account_custome_name_et);
        mCreateAcountView = findViewById(R.id.create_account_btn);
        mCreateAcountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPasswordEt.getText() == null || TextUtils.isEmpty(mPasswordEt.getText().toString())){
                    Toast.makeText(CreateAccountActivity.this, R.string.blank_password, Toast.LENGTH_LONG).show();
                    return;
                }
                new CreateAddressAsycTask(CreateAccountActivity.this, mCoinType,
                        mPasswordEt.getText().toString(),
                        mAccountNameEt.getText() != null ? mAccountNameEt.getText().toString() : "").execute();
            }
        });
    }
}
