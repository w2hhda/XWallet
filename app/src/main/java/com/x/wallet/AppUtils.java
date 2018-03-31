package com.x.wallet;

import android.content.Context;
import android.os.Build;

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
    public static final String HAS_TOKEN_KEY ="has_token";
    public static final String TOKEN_DATA ="token_data";

    public static final String APP_TAG = "XWallet";

    public static final int CREATE_ADDRESS_FAILED_OTHER = -1;
    public static final int CREATE_ADDRESS_OK = 0;
    public static final int CREATE_ADDRESS_FAILED_ACCOUNTNAME_SAME = 1;
    public static final int CREATE_ADDRESS_FAILED_ADDRESS_EXIST = 2;

    public static final int HAS_TOKEN = 1;
}
