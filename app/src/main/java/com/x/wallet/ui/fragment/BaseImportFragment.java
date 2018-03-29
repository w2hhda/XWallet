package com.x.wallet.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.ui.view.AccountNameView;
import com.x.wallet.ui.view.PrivacyPolicyView;
import com.x.wallet.ui.view.SetPasswordView;

/**
 * Created by wuliang on 18-3-16.
 */

public class BaseImportFragment extends Fragment {
    protected int mCoinType;
    protected AccountNameView mAccountNameView;
    protected SetPasswordView mSetPasswordView;
    protected PrivacyPolicyView mPrivacyPolicyView;
    protected View mImportAccountView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void initCommonView(View view){
        mCoinType = getArguments().getInt(AppUtils.COIN_TYPE);

        mAccountNameView = view.findViewById(R.id.account_name_view);
        mSetPasswordView = view.findViewById(R.id.set_password_view);
        mImportAccountView = view.findViewById(R.id.import_account_btn);

        mPrivacyPolicyView = view.findViewById(R.id.privacy_policy_container);
        mPrivacyPolicyView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mImportAccountView.setEnabled(isChecked);
            }
        });
    }
}
