package com.x.wallet;

import android.content.ContentValues;

import com.x.wallet.btclibrary.AccountData;
import com.x.wallet.db.DbUtils;

/**
 * Created by wuliang on 18-3-13.
 */

public class AppUtils {
    public static final int ACCOUNT_ACTION_TYPE_NEW = 0;
    public static final int ACCOUNT_ACTION_TYPE_IMPORT = 1;

    public static final String[] COIN_ARRAY = {"BTC", "ETH"};

    public static ContentValues createContentValues(AccountData accountData) {
        ContentValues values = new ContentValues();
        values.put(DbUtils.DbColumns.ADDRESS, accountData.getAddress());
        values.put(DbUtils.DbColumns.NAME, accountData.getAccountName());
        values.put(DbUtils.DbColumns.COIN_NAME, accountData.getCoinName());
        values.put(DbUtils.DbColumns.ENCRYPT_SEED, accountData.getEncryptSeed());
        values.put(DbUtils.DbColumns.ENCRYPT_MNEMONIC, accountData.getEncryptMnemonic());
        return values;
    }

    public interface COINTYPE{
        int COIN_BTC = 0;
        int COIN_ETH = 1;
    }
}
