package com.x.wallet.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.ui.view.AccountNameView;
import com.x.wallet.ui.view.CoinNameView;
import com.x.wallet.ui.view.SetPasswordView;

/**
 * Created by wuliang on 18-3-16.
 */

public class BaseImportFragment extends Fragment {
    protected int mCoinType;
    protected CoinNameView mCoinNameView;
    protected AccountNameView mAccountNameView;
    protected SetPasswordView mSetPasswordView;
    protected View mImportAccountView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void initCommonView(View view){
        mCoinType = getArguments().getInt(AppUtils.COIN_TYPE);
        mCoinNameView = view.findViewById(R.id.coin_name_view);
        mCoinNameView.setCoinName(AppUtils.COIN_ARRAY[mCoinType]);

        mAccountNameView = view.findViewById(R.id.account_name_view);
        mSetPasswordView = view.findViewById(R.id.set_password_view);
        mImportAccountView = view.findViewById(R.id.import_account_btn);
    }
}
