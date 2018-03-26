package com.x.wallet;

import android.content.ContentValues;

import com.x.wallet.lib.common.AccountData;
import com.x.wallet.db.DbUtils;
import com.x.wallet.lib.eth.data.EthAccountData;

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

    public interface IMPORTTYPE{
        int IMPORT_TYPE_MNEMONIC = 0;
        int IMPORT_TYPE_KEY = 1;
        int IMPORT_TYPE_KEYSTORE = 2;
    }

    public static final String ACTION_TYPE = "action_type";
    public static final String COIN_TYPE  = "coin_type";
    public static final String ACCOUNT_DATA = "account_data";

    public static final String APP_TAG = "XWallet";
}
