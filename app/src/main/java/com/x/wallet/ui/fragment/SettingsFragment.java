package com.x.wallet.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.R;

/**
 * Created by wuliang on 18-3-13.
 */

public class SettingsFragment extends Fragment{
    private View mManageAccountItem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        mManageAccountItem = view.findViewById(R.id.manage_account_rl);
        mManageAccountItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action.MANAGE_ALL_ACCOUNT_ACTION");
                startActivity(intent);
            }
        });
        return view;
    }
}
