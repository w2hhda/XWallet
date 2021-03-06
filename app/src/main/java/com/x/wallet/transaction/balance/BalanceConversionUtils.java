package com.x.wallet.transaction.balance;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by wuliang on 18-3-26.
 */

public class BalanceConversionUtils {
    public static final String ETH_TO_USD_VALUE_PREF_KEY = "eth_to_usd_value";
    public static double mEthToUsd = 0;

    private static RateUpdateListener mRateUpdateListener;
    private static Set<RateUpdateListener> mListenerSet = new HashSet<>();

    public static void init(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        mEthToUsd = Double.parseDouble(preferences.getString(ETH_TO_USD_VALUE_PREF_KEY, "0"));
    }

    public static void write(double value){
        mEthToUsd = value;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ETH_TO_USD_VALUE_PREF_KEY, String.valueOf(value));
        editor.apply();
    }

    public static void setRateUpdateListener(RateUpdateListener rateUpdateListener) {
        mRateUpdateListener = rateUpdateListener;
    }

    public static void responseToListener() {
        if(mRateUpdateListener != null){
            mRateUpdateListener.onRateUpdate();
        }
    }

    public static void registerListener(RateUpdateListener listener){
        mListenerSet.add(listener);
    }

    public static void unRegisterListener(RateUpdateListener listener){
        mListenerSet.remove(listener);
    }

    public static void handleListener(){
        for(RateUpdateListener listener : mListenerSet){
            listener.onRateUpdate();
        }
    }

    public static void clearListener() {
        setRateUpdateListener(null);
        Log.i(AppUtils.APP_TAG, "BalanceConversionUtils clearListener mListenerSet.size = " + mListenerSet.size());
        mListenerSet.clear();
    }

    public interface RateUpdateListener{
        void onRateUpdate();
    }
}
