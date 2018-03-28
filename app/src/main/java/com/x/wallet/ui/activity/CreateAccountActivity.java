package com.x.wallet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.transaction.address.CreateAddressAsycTask;
import com.x.wallet.ui.view.AccountNameView;
import com.x.wallet.ui.view.PrivacyPolicyView;
import com.x.wallet.ui.view.SetPasswordView;


/**
 * Created by wuliang on 18-3-13.
 */

public class CreateAccountActivity extends WithBackAppCompatActivity {
    private AccountNameView mAccountNameView;
    private SetPasswordView mSetPasswordView;

    private PrivacyPolicyView mPrivacyPolicyView;

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
        mAccountNameView = findViewById(R.id.account_name_view);
        mSetPasswordView = findViewById(R.id.set_password_view);

        mCreateAcountView = findViewById(R.id.create_account_btn);
        mCreateAcountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mAccountNameView.isAccountNameOk()){
                    Toast.makeText(CreateAccountActivity.this, R.string.account_name_error, Toast.LENGTH_LONG).show();
                    return;
                }

                int passwordCheckResult = mSetPasswordView.isPasswordOk();
                switch (passwordCheckResult){
                    case SetPasswordView.PasswordErrorType.BLANK:
                        Toast.makeText(CreateAccountActivity.this, R.string.password_error_blank, Toast.LENGTH_LONG).show();
                        return;
                    case SetPasswordView.PasswordErrorType.NOT_THE_SAME:
                        Toast.makeText(CreateAccountActivity.this, R.string.password_error_not_the_same, Toast.LENGTH_LONG).show();
                        return;
                    case SetPasswordView.PasswordErrorType.SHORT:
                        Toast.makeText(CreateAccountActivity.this, R.string.password_error_short, Toast.LENGTH_LONG).show();
                        return;
                    case SetPasswordView.PasswordErrorType.OK:
                        new CreateAddressAsycTask(CreateAccountActivity.this, mCoinType,
                                mSetPasswordView.getPassword(),
                                mAccountNameView.getAccountName()).execute();
                        return;
                }

            }
        });

        mPrivacyPolicyView = findViewById(R.id.privacy_policy_container);
        mPrivacyPolicyView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mCreateAcountView.setEnabled(isChecked);
            }
        });
    }
}
