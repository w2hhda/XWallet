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

    public interface TxTableColumns{
        String _ID = "_id";                             //0
        String TX_HASH = "tx_hash";                     //1
        String TIME_STAMP = "timestamp";                //2
        String NONCE = "nonce";                         //3
        String FROM_ADDRESS = "from_address";           //4
        String TO_ADDRESS = "to_address";               //5
        String VALUE = "value";                         //6
        String GAS_LIMIT = "gas_limit";                 //7
        String GAS_PRICE = "gas_price";                 //8
        String IS_ERROR = "is_error";                   //9
        String TX_RECEIPT_STATUS = "tx_receipt_status"; //10
        String INPUT_DATA = "input_data";               //11
        String GAS_USED = "gas_used";                   //12
        String CONTRACT_ADDRESS = "contract_address";   //13
        String TOKEN_SYMBOL = "token_symbol";           //14
        String TOKEN_NAME = "token_name";               //15
        String TOKEN_DECIMALS = "token_decimals";       //16
        String BLOCK_NUMBER = "block_number";           //17

    }

    public static final String UPDATE_TOKEN_SELECTION = DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " = ? AND " + DbUtils.TokenTableColumns.SYMBOL + " = ?";
    public static final String UPDATE_TX_SELECTION = TxTableColumns.TX_HASH + " = ？";

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

    public static boolean isAlreadyExistToken(String selection, String[] selectionArgs){
        Cursor cursor = null;
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI_TOKEN,
                    new String[]{DbUtils.TokenTableColumns._ID},
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

    public static int deleteTokenForAccount(long accountId){
        String selection = DbUtils.TokenTableColumns.ACCOUNT_ID + " = ?";
        return XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                .delete(XWalletProvider.CONTENT_URI_TOKEN, selection, new String[]{Long.toString(accountId)});
    }

    public static String queryAllEthAddress(){
        Cursor cursor = null;
        try{
            //1.query from db
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI, new String[]{DbUtils.DbColumns.ADDRESS}, null, null, null);
            if(cursor != null && cursor.getCount() > 0){
                StringBuilder builder = new StringBuilder("");
                while (cursor.moveToNext()){
                    builder.append(cursor.getString(0));
                    builder.append(",");
                }
                String address = builder.toString();
                return address.substring(0, address.length() -1);
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return null;
    }

    public static Cursor queryAllTokenAddress(){
        return XWalletApplication.getApplication().getContentResolver().query(
                XWalletProvider.CONTENT_URI_TOKEN, new String[]{DbUtils.TokenTableColumns.ACCOUNT_ADDRESS}, null, null, null);
    }

    public static long queryEthAccountId(String address){
        String selection = DbColumns.ADDRESS + " = ?";
        Cursor cursor = null;
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI,
                    new String[]{DbColumns._ID},
                    selection,
                    new String[]{address}, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                return cursor.getLong(0);
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return -1;
    }

    public static boolean isTxExist(String txHash){
        Cursor cursor = null;
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI_TRANSACTION,
                    null,
                    null,
                    null, null);
            if(cursor != null && cursor.getCount() > 0){
                while (cursor.moveToNext()) {
                    String hash = cursor.getString(cursor.getColumnIndex(TxTableColumns.TX_HASH));
                    if (hash != null && hash.equalsIgnoreCase(txHash)) {
                        return true;
                    }
                }
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return false;
    }

    public static boolean isTokenNeedUpdate(String txHash, String blockNumber){
        Cursor cursor = null;
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI_TRANSACTION,
                    null,
                    null,
                    null, null);
            if(cursor != null && cursor.getCount() > 0){
                while (cursor.moveToNext()) {
                    String hash = cursor.getString(cursor.getColumnIndex(TxTableColumns.TX_HASH));
                    String blockNum = cursor.getString(cursor.getColumnIndex(TxTableColumns.BLOCK_NUMBER));
                    if (hash != null && hash.equalsIgnoreCase(txHash)) {
                        if (blockNum != null && blockNum.equals(blockNumber)) {
                            String value = cursor.getString(cursor.getColumnIndex(TxTableColumns.VALUE));
                            //value = 0, need to insert contract address .etc when load token tx list.
                            return value.equals("0");
                        }
                    }
                }
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return false;
    }

    public static boolean isTxNeedUpdate(String txHash, String blockNumber){
        Cursor cursor = null;
        try {
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI_TRANSACTION,
                    null,
                    null,
                    null, null);
            if(cursor != null && cursor.getCount() > 0){
                while (cursor.moveToNext()) {
                    // hash exits & block number = 0, means new tx, need to update
                    String hash = cursor.getString(cursor.getColumnIndex(TxTableColumns.TX_HASH));
                    String blockNum = cursor.getString(cursor.getColumnIndex(TxTableColumns.BLOCK_NUMBER));

                    if (hash != null && hash.equalsIgnoreCase(txHash)){
                        if (blockNum != null && !blockNum.equals(blockNumber)) {
                            //new tx
                            return true;
                        }
                    }

                }
            }
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return false;
    }
}
