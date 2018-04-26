package com.x.wallet.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.x.wallet.AppUtils;
import com.x.wallet.MainActivity;
import com.x.wallet.R;
import com.x.wallet.ui.activity.CurrencyActivity;
import com.x.wallet.ui.activity.ServicePolicyActivity;
import com.x.wallet.ui.view.ChangeCurrencyPrefView;
import com.x.wallet.ui.view.PrivacyPolicyView;

/**
 * Created by wuliang on 18-3-13.
 */

public class SettingsFragment extends Fragment{

    private static final int CHOOSE_CURRENCY_REQUEST_CODE = 1;
    private static final int SET_PIN_REQUEST_CODE = 2;
    private static final int CONFIRM_PIN_REQUEST_CODE = 3;
    private ChangeCurrencyPrefView mChangeCurrencyPref;
    private Switch mSwitch;

    public static final String TO_CONFONFIRM_PIN = "SettingsFragment.to.confirm.pin";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        View manageAccountItem = view.findViewById(R.id.manage_account_rl);
        manageAccountItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action.MANAGE_ALL_ACCOUNT_ACTION");
                getActivity().startActivityForResult(intent, MainActivity.MANAGE_ALL_ACCOUNT_REQUEST_CODE);
            }
        });

        View servicePolicyItem = view.findViewById(R.id.service_aggrement_rl);
        PrivacyPolicyView.setOnClickListener(getContext(), servicePolicyItem, ServicePolicyActivity.TYPE_SERVICE);

        View privacyPolicyItem = view.findViewById(R.id.privacy_terms_rl);
        PrivacyPolicyView.setOnClickListener(getContext(), privacyPolicyItem, ServicePolicyActivity.TYPE_PRIVACY);

        mChangeCurrencyPref = view.findViewById(R.id.change_currency_pref);
        mChangeCurrencyPref.updateCurrentCurrencyText();
        mChangeCurrencyPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action.CHOOSE_CURRENCY_ACTION");
                SettingsFragment.this.startActivityForResult(intent, CHOOSE_CURRENCY_REQUEST_CODE);
            }
        });
        initPinView(view);

        View favoriteAddress = view.findViewById(R.id.address_rl);
        favoriteAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.x.wallet.action.SEE_FAVORITE_ADDRESS_ACTION");
                SettingsFragment.this.startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(CHOOSE_CURRENCY_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK){
            mChangeCurrencyPref.updateCurrentCurrencyText(data.getStringExtra(CurrencyActivity.CHOOSE_CURRENCY));
        }
        if (SET_PIN_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK){
            mSwitch.setChecked(true);
            String code = data.getStringExtra(AppUtils.PIN_TAG);
            String md5Value = AppUtils.getStringMD5(code);
            AppUtils.log("try to set pin");
            AppUtils.setPin(md5Value);
        }
        if (CONFIRM_PIN_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK){
            AppUtils.log("confirm pin ok.");
            mSwitch.setChecked(false);
            AppUtils.setPin("");
        }
    }

    private void initPinView(View view){
        mSwitch = view.findViewById(R.id.pin_switch);
        mSwitch.setChecked(AppUtils.hasPin());
        //View pinLayout = view.findViewById(R.id.set_pin_layout);
        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppUtils.hasPin()){
                    AppUtils.log("try to confirm pin code");
                    Intent intent = new Intent("com.x.wallet.action.CONFIRM_PIN_ACTION");
                    intent.putExtra(AppUtils.CONFIRM_PIN_CODE, AppUtils.getPin());
                    intent.putExtra(TO_CONFONFIRM_PIN, true);
                    startActivityForResult(intent, CONFIRM_PIN_REQUEST_CODE);
                }else {
                    AppUtils.log("try to set pin code");
                    Intent intent = new Intent("com.x.wallet.action.SET_PIN_ACTION");
                    startActivityForResult(intent, SET_PIN_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mSwitch.setChecked(AppUtils.hasPin());
    }
}