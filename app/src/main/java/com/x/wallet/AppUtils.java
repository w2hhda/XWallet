package com.x.wallet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wuliang on 18-3-13.
 */

public class AppUtils {
    public static final int ACCOUNT_ACTION_TYPE_NEW = 0;
    public static final int ACCOUNT_ACTION_TYPE_IMPORT = 1;

    public static final String[] COIN_ARRAY = {"BTC", "ETH"};

    public static int getMnemonicType(String mnemonicTypeText) {
        return 0;
    }

    public static int getColor(Context context, int colorId) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return context.getResources().getColor(colorId);
        } else{
            return context.getResources().getColor(colorId, null);
        }
    }

    public interface IMPORTTYPE{
        int IMPORT_TYPE_MNEMONIC = 0;
        int IMPORT_TYPE_KEY = 1;
        int IMPORT_TYPE_KEYSTORE = 2;
    }

    public static final String ACTION_TYPE = "action_type";
    public static final String COIN_TYPE  = "coin_type";
    public static final String ACCOUNT_DATA = "account_data";
    public static final String ADDRESS_URI = "address_uri";
    public static final String TRANSACTION_ITEM = "transaction_item";
    public static final String ACCOUNT_ID ="account_id";
    public static final String ACCOUNT_ADDRESS = "account_address";
    public static final String TOKEN_DATA ="token_data";
    public static final String ACCOUNT_TYPE = "account_type";
    public static final String TX_LIST_SYNCED = "tx_list_synced:";
    public static final String BACKGROUND_TAG = "background_tag";
    public static final String PIN_TAG = "pin_tag";
    public static final String SET_PIN_CODE = "set_pin";
    public static final String CONFIRM_PIN_CODE = "confirm_pin_code";
    public static final String ADDRESS_ITEM = "address_item";
    public static final String EXTRA_ADDRESS = "extra_address";

    public static final String APP_TAG = "XWallet";

    public static final int CREATE_ADDRESS_FAILED_OTHER = -1;
    public static final int CREATE_ADDRESS_OK = 0;
    public static final int CREATE_ADDRESS_FAILED_ACCOUNTNAME_SAME = 1;
    public static final int CREATE_ADDRESS_FAILED_ADDRESS_EXIST = 2;

    public static final int HAS_BACKUP = 1;

    public static void log(String msg){
        Log.i("CoinPay", msg);
    }

    public static String formatDate(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(timeStamp * 1000L);
        return sdf.format(date);
    }

    public static String formatDate(String timeStamp){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(Long.parseLong(timeStamp) * 1000L);
        return sdf.format(date);
    }


    public static Boolean isValideNumber(String str){
        Pattern pattern = Pattern.compile("([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9])");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isFirstTimeToSyncTxList(String address, String syncAddress){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        String syncNames = preferences.getString(TX_LIST_SYNCED + address, "");
        return !syncNames.contains(syncAddress);
    }

    public static void updateSyncAddress(String address, String syncAddress){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        String syncNames = preferences.getString(TX_LIST_SYNCED + address, "");
        if (!syncNames.contains(syncAddress)){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(TX_LIST_SYNCED + address, syncNames + " " + syncAddress);
            editor.apply();
        }
    }

    public static View getStubView(Activity activity, int stubId, int viewId) {
        View view = activity.findViewById(viewId);
        if (view == null) {
            ViewStub stub = (ViewStub) activity.findViewById(stubId);
            view = stub.inflate();
        }

        return view;
    }

    public static String getStringMD5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean startFromBg(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        return preferences.getBoolean(BACKGROUND_TAG, true);
    }

    public static void setBackground(boolean background){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(BACKGROUND_TAG, background);
        editor.apply();
    }

    public static boolean hasPin(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        return !TextUtils.isEmpty(preferences.getString(PIN_TAG, ""));
    }

    public static void setPin(String pin){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        if (TextUtils.isEmpty(pin)){
            editor.remove(PIN_TAG);
        }else {
            editor.putString(PIN_TAG, pin);
        }
        editor.apply();
    }

    public static String getPin(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        return preferences.getString(PIN_TAG,null);
    }
}
