package com.x.wallet.transaction.usdtocny;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.UsdCnyBean;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
        requestCnyToUsd();
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

    public static void requestCnyToUsd(){
            try{
                if(isNeedRequest()){
                    EtherscanAPI.getInstance().getPriceConversionRates("CNY", new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(AppUtils.APP_TAG, "UsdToCnyHelper onFailure for requestCnyToUsd" , e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            ResponseBody body2 = response.body();
                            if (body2 != null){
                                UsdCnyBean usdCnyBean = new Gson().fromJson(body2.string(), UsdCnyBean.class);
                                write(usdCnyBean.getRates().getCNY());
                                Log.i(AppUtils.APP_TAG, "UsdToCnyHelper onResponse requestCnyToUsd UsdToCny = " + usdCnyBean.getRates().getCNY());
                            }
                        }
                    });
                }
            } catch (Exception e){
                Log.e(AppUtils.APP_TAG, "UsdToCnyHelper requestCnyToUsd exception" , e);
            }
    }
}
