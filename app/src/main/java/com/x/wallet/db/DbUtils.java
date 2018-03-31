package com.x.wallet.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.x.wallet.XWalletApplication;
import com.x.wallet.lib.common.AccountData;

/**
 * Created by wuliang on 18-3-14.
 */

public class DbUtils {
    public interface DbColumns{
        String _ID = "_id";
        String ADDRESS = "address";
        String NAME = "name";
        String COIN_NAME = "coin_name";
        String COIN_TYPE = "coin_type";
        String ENCRYPT_SEED = "encrypt_seed";
        String ENCRYPT_MNEMONIC = "encrypt_mnemonic";
        String ENCRYPT_PRIV_KEY = "encrypt_priv_key";
        String KEYSTORE = "keystore";
        String BALANCE = "balance";
        String HAS_TOKEN = "has_token";
    }

    public interface TokenTableColumns{
        String _ID = "_id";                          //0
        String ACCOUNT_ID = "account_id";            //1
        String ACCOUNT_ADDRESS = "account_address";  //2
        String ID_IN_ALL = "id_in_all";              //3
        String NAME = "name";                        //4
        String SYMBOL = "symbol";                    //5
        String DECIMALS = "decimals";                //6
        String CONTRACT_ADDRESS = "contract_address"; //7
        String BALANCE = "balance";                  //8
        String RATE = "rate"; //usdprice             //9
    }

    public static final String UPDATE_TOKEN_SELECTION = DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " = ? AND " + DbUtils.TokenTableColumns.SYMBOL + " = ?";

    public static ContentValues createContentValues(AccountData accountData) {
        ContentValues values = new ContentValues();
        values.put(DbUtils.DbColumns.ADDRESS, accountData.getAddress());
        values.put(DbUtils.DbColumns.NAME, accountData.getAccountName());
        values.put(DbUtils.DbColumns.COIN_NAME, accountData.getCoinName());
        values.put(DbUtils.DbColumns.COIN_TYPE, accountData.getCoinType());
        if(!TextUtils.isEmpty(accountData.getEncryptSeed())){
            values.put(DbUtils.DbColumns.ENCRYPT_SEED, accountData.getEncryptSeed());
        }

        if(!TextUtils.isEmpty(accountData.getEncryptMnemonic())){
            values.put(DbUtils.DbColumns.ENCRYPT_MNEMONIC, accountData.getEncryptMnemonic());
        }

        if(!TextUtils.isEmpty(accountData.getEncryptPrivKey())){
            values.put(DbUtils.DbColumns.ENCRYPT_PRIV_KEY, accountData.getEncryptPrivKey());
        }

        if(!TextUtils.isEmpty(accountData.getKeyStore())){
            values.put(DbUtils.DbColumns.KEYSTORE, accountData.getKeyStore());
        }
        return values;
    }

    public static boolean isAccountNameExist(String accountName){
        return isAlreadyExist(DbColumns.NAME + " = ?", new String[]{accountName});
    }

    public static boolean isAddressExist(String address){
        return isAlreadyExist(DbColumns.ADDRESS + " = ?", new String[]{address});
    }

    private static boolean isAlreadyExist(String selection, String[] selectionArgs){
        Cursor cursor = null;
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI,
                    new String[]{DbUtils.DbColumns._ID},
                    selection,
                    selectionArgs, null);
            if(cursor != null && cursor.getCount() > 0){
                return true;
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return false;
    }
}
