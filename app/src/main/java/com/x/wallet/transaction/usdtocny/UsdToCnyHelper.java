package com.x.wallet.transaction.usdtocny;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.x.wallet.XWalletApplication;

import java.util.Calendar;

/**
 * Created by wuliang on 18-3-31.
 */

public class UsdToCnyHelper {
    public static double mUsdToCny = 0;
    public static long mDate = 0;
    public static final String USD_TO_CNY_VALUE_PREF_KEY = "usd_to_cny_value";
    public static final String USD_TO_CNY_DATE_PREF_KEY = "usd_to_cny_date";

    public static void init(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        mUsdToCny = Double.parseDouble(preferences.getString(USD_TO_CNY_VALUE_PREF_KEY, "0"));
        mDate = preferences.getLong(USD_TO_CNY_DATE_PREF_KEY, 0);
    }

    public static void write(double value){
        mUsdToCny = value;
        mDate = System.currentTimeMillis();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USD_TO_CNY_VALUE_PREF_KEY, String.valueOf(value));
        editor.putLong(USD_TO_CNY_DATE_PREF_KEY, mDate);
        editor.apply();
    }

    public static boolean isNeedRequest(){
        return mUsdToCny <= 0 || isDateWrong();
    }

    private static boolean isDateWrong(){
        if(mDate > 0){
            Calendar old = Calendar.getInstance();
            old.setTimeInMillis(mDate);
            int oldYear = old.get(Calendar.YEAR);
            int oldMonth = (old.get(Calendar.MONTH) + 1);
            int oldDAY = old.get(Calendar.DAY_OF_MONTH);

            Calendar now = Calendar.getInstance();
            int currentYear = now.get(Calendar.YEAR);
            int currentMonth = (now.get(Calendar.MONTH) + 1);
            int currentDAY = now.get(Calendar.DAY_OF_MONTH);

            if(oldYear != currentYear) return true;
            if(oldMonth != currentMonth) return true;
            return currentDAY != oldDAY;
        }
        return true;
    }
}
