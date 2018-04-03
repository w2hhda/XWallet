package com.x.wallet.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.R;
import com.x.wallet.ui.activity.ServicePolicyActivity;

/**
 * Created by wuliang on 18-3-13.
 */

public class SettingsFragment extends Fragment{
    private View mManageAccountItem;
    private View mServicePolicyItem;
    private View mPrivacyPolicyItem;

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

        //TO-DO: need to change {@
        mServicePolicyItem = view.findViewById(R.id.mark_rl);
        mServicePolicyItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action_SEE_SERVICE_POLICYL");
                intent.putExtra(ServicePolicyActivity.TYPE_TAG, ServicePolicyActivity.TYPE_SERVICE);
                getContext().startActivity(intent);
            }
        });

        mPrivacyPolicyItem = view.findViewById(R.id.about_us_rl);
        mPrivacyPolicyItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action_SEE_SERVICE_POLICYL");
                intent.putExtra(ServicePolicyActivity.TYPE_TAG, ServicePolicyActivity.TYPE_PRIVACY);
                getContext().startActivity(intent);
            }
        });
        //End TO-DO @}
        return view;
    }
}
