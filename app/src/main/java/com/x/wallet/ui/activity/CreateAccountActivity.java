package com.x.wallet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.transaction.address.CreateAddressAsycTask;
import com.x.wallet.ui.view.AccountNameView;
import com.x.wallet.ui.view.CoinNameView;
import com.x.wallet.ui.view.SetPasswordView;


/**
 * Created by wuliang on 18-3-13.
 */

public class CreateAccountActivity extends BaseAppCompatActivity {
    private CoinNameView mCoinNameView;
    private AccountNameView mAccountNameView;
    private SetPasswordView mSetPasswordView;
    private View mCreateAcountView;

    private int mCoinType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle();

        setContentView(R.layout.create_account_activity);

        initView();
    }

    private void setTitle(){
        Intent intent = getIntent();
        int index = intent.getIntExtra(AppUtils.COIN_TYPE, 0);
        mCoinType = index;
        this.setTitle(this.getString(R.string.create_account_title, AppUtils.COIN_ARRAY[index]));
    }

    private void initView(){
        mCoinNameView = findViewById(R.id.coin_name_view);
        mCoinNameView.setCoinName(AppUtils.COIN_ARRAY[mCoinType]);

        mAccountNameView = findViewById(R.id.account_name_view);
        mSetPasswordView = findViewById(R.id.set_password_view);

        mCreateAcountView = findViewById(R.id.create_account_btn);
        mCreateAcountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mSetPasswordView.getPassword())){
                    Toast.makeText(CreateAccountActivity.this, R.string.blank_password, Toast.LENGTH_LONG).show();
                    return;
                }
                new CreateAddressAsycTask(CreateAccountActivity.this, mCoinType,
                        mSetPasswordView.getPassword(),
                        mAccountNameView.getAccountName()).execute();
            }
        });
    }
}
