package com.x.wallet.transaction.usdtocny;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.UsdCnyBean;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by wuliang on 18-3-31.
 */

public class UsdToCnyHelper {
    private static final String TAG = "UsdToCnyHelper";
    public static double mUsdToCny = 0;
    public static long mDate = 0;
    public static final String USD_TO_CNY_VALUE_PREF_KEY = "usd_to_cny_value";
    public static final String USD_TO_CNY_DATE_PREF_KEY = "usd_to_cny_date";

    public static final String CURRENT_CURRENCY_KEY = "current_currency";
    public static final String CURRENT_CURRENCY_UNIT_KEY = "current_currency_unit";
    public static String mCurrentCurrency;
    public static String mCurrencyUnit;

    public static void init(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        mCurrentCurrency = preferences.getString(CURRENT_CURRENCY_KEY, "CNY");
        mCurrencyUnit = preferences.getString(CURRENT_CURRENCY_UNIT_KEY, "¥");
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

    private static void requestCnyToUsd(){
            try{
                if(isNeedRequest()){
                    EtherscanAPI.getInstance().getPriceConversionRates(mCurrentCurrency, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(AppUtils.APP_TAG, "UsdToCnyHelper onFailure for requestCnyToUsd" , e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            ResponseBody body2 = response.body();
                            if (body2 != null){
                                double result = getRate(body2.string(), mCurrentCurrency);
                                Log.i(AppUtils.APP_TAG, "UsdToCnyHelper onResponse requestCnyToUsd UsdToCny = " + result);
                                if(result > 0){
                                    write(result);
                                }
                            }
                        }
                    });
                }
            } catch (Exception e){
                Log.e(AppUtils.APP_TAG, "UsdToCnyHelper requestCnyToUsd exception" , e);
            }
    }

    public static double requestCurrencyToUsd(String chooseCurrency){
        try{
            if(chooseCurrency.equals("USD")) return 1;
//            HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
//            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(logInterceptor).build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.fixer.io/")
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    //.client(okHttpClient)
                    .build();
            GetCurrencyToUsdService getCurrencyToUsdService = retrofit.create(GetCurrencyToUsdService.class);
            Log.i(TAG, "UsdToCnyHelper requestCurrencyToUsd mCurrentCurrency = " + chooseCurrency);
            retrofit2.Call<ResponseBody> call = getCurrencyToUsdService.getCurrencyToUsd("USD", chooseCurrency);
            retrofit2.Response<ResponseBody>  response = call.execute();
            if(response == null){
                return -1;
            }
            ResponseBody body = response.body();
            if(body == null) return -1;
            String result = body.string();
            Log.i(TAG, "UsdToCnyHelper requestCurrencyToUsd result = " + result);
            return getRate(result, chooseCurrency);
        } catch (Exception e){
            Log.e(TAG, "UsdToCnyHelper requestCurrencyToUsd exception" , e);
        }
        return -1;
    }

    private static double getRate(String result, String chooseCurrency){
        try {
            if(!TextUtils.isEmpty(result)){
                JSONObject jsonObject = new JSONObject(result);
                if(jsonObject.has("rates")){
                    JSONObject ratesJsonObject = jsonObject.getJSONObject("rates");
                    if(ratesJsonObject != null && ratesJsonObject.has(chooseCurrency)){
                        Object last = ratesJsonObject.get(chooseCurrency);
                        if(last != null){
                            return (double) last;
                        }
                    }
                }
            }
        } catch (Exception e){

        }
        return -1;
    }

    public static String getChooseCurrency() {
        return mCurrentCurrency;
    }

    public static String getChooseCurrencyUnit() {
        return mCurrencyUnit;
    }

    public static void updateCurrentCheck(String currentCurrency, String currencyUnit) {
        if(!mCurrentCurrency.equals(currentCurrency)){
            mCurrentCurrency = currentCurrency;
            mCurrencyUnit = currencyUnit;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(CURRENT_CURRENCY_KEY, currentCurrency);
            editor.putString(CURRENT_CURRENCY_UNIT_KEY, currencyUnit);
            editor.apply();
        }
    }
}
