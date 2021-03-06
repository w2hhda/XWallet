package com.x.wallet.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.lib.common.AccountData;
import com.x.wallet.lib.common.LibUtils;

/**
 * Created by wuliang on 18-3-14.
 */

public class DbUtils {
    public static final int IS_SYNCED = 1;
    public static final int NOT_SYNCED = 0;

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
        String HAS_BACKUP = "has_backup";
        String PUB_KEY = "pub_key";
        String IS_SYNCED = "is_synced";
    }

    public interface TokenTableColumns{
        String _ID = "_id";                          //0
        String ACCOUNT_ID = "account_id";            //1
        String ACCOUNT_ADDRESS = "account_address";  //2
        String NAME = "name";                        //3
        String SYMBOL = "symbol";                    //4
        String DECIMALS = "decimals";                //5
        String CONTRACT_ADDRESS = "contract_address"; //6
        String BALANCE = "balance";                  //7
        String RATE = "rate"; //usdprice             //8
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

    public interface AddressTableColumns{
        String _ID = "_id";                             //0
        String ADDRESS = "address";                     //1
        String ADDRESS_TYPE = "address_type";           //2
        String NAME = "address_name";                   //3

    }

    public static final String UPDATE_TOKEN_SELECTION = DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " = ? AND " + DbUtils.TokenTableColumns.SYMBOL + " = ?";
    private static final String COINTYPE_SELECTION = DbColumns.COIN_TYPE + " = ?";
    private static final String[] COINTYPE_SELECTION_ETH = new String[]{String.valueOf(LibUtils.COINTYPE.COIN_ETH)};
    private static final String[] COINTYPE_SELECTION_BTC = new String[]{String.valueOf(LibUtils.COINTYPE.COIN_BTC)};
    public static final String ADDRESS_SELECTION = DbColumns.ADDRESS + " = ?";

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

        if(!TextUtils.isEmpty(accountData.getPubKey())){
            values.put(DbColumns.PUB_KEY, accountData.getPubKey());
        }

        values.put(DbColumns.IS_SYNCED, accountData.isSyncComplete() ? IS_SYNCED : NOT_SYNCED);
        return values;
    }

    public static boolean isAccountNameExist(String accountName){
        return isAlreadyExist(DbColumns.NAME + " = ?", new String[]{accountName});
    }

    public static boolean isAddressExist(String address){
        return isAlreadyExist(ADDRESS_SELECTION, new String[]{address});
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
                    XWalletProvider.CONTENT_URI, new String[]{DbUtils.DbColumns.ADDRESS},
                    COINTYPE_SELECTION, COINTYPE_SELECTION_ETH, null);
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

    public static Cursor queryAllEthAddressToCursor(){
        return XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                XWalletProvider.CONTENT_URI, new String[]{DbUtils.DbColumns.ADDRESS}, COINTYPE_SELECTION, COINTYPE_SELECTION_ETH, null);
    }

    public static Cursor queryAllTokenAddress(){
        return XWalletApplication.getApplication().getContentResolver().query(
                XWalletProvider.CONTENT_URI_TOKEN, new String[]{DbUtils.TokenTableColumns.ACCOUNT_ADDRESS}, null, null, null);
    }

    public static long queryEthAccountId(String address){
        Cursor cursor = null;
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI,
                    new String[]{DbColumns._ID},
                    ADDRESS_SELECTION,
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
        final String selection = TxTableColumns.TX_HASH + " = ?";
        final String[] selectionArgs = new String[]{txHash};
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI_TRANSACTION,
                    null,
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

    public static boolean isTokenNeedUpdate(String txHash, String blockNumber){
        Cursor cursor = null;
        final String selection = TxTableColumns.TX_HASH + " = ? AND " + TxTableColumns.BLOCK_NUMBER + " = ? AND " + TxTableColumns.VALUE + " = ?";
        final String[] selectionArgs = new String[]{txHash, blockNumber, "0"};
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI_TRANSACTION,
                    null,
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

    public static boolean isTxNeedUpdate(String txHash, String blockNumber){
        Cursor cursor = null;
        final String selection = TxTableColumns.TX_HASH + " = ? AND " + TxTableColumns.BLOCK_NUMBER + " != ?";
        final String[] selectionArgs = new String[]{txHash, blockNumber};
        try {
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI_TRANSACTION,
                    null,
                    selection,
                    selectionArgs, null);
            if(cursor != null && cursor.getCount() > 0){
                return true;
            }
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return false;
    }

    public static Uri insertTokenIntoDb(long accountId, String accountAddress,
                                         String name, String symbol, int decimals,
                                         String contractAddress, String balance, String rate) {
        ContentValues values = new ContentValues();
        values.put(DbUtils.TokenTableColumns.ACCOUNT_ID, accountId);
        values.put(DbUtils.TokenTableColumns.ACCOUNT_ADDRESS, accountAddress);
        values.put(DbUtils.TokenTableColumns.NAME, name);
        values.put(DbUtils.TokenTableColumns.SYMBOL, symbol);
        values.put(DbUtils.TokenTableColumns.DECIMALS, decimals);
        values.put(DbUtils.TokenTableColumns.CONTRACT_ADDRESS, contractAddress);
        values.put(DbUtils.TokenTableColumns.BALANCE, balance);
        values.put(DbUtils.TokenTableColumns.RATE, rate);
        return XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                .insert(XWalletProvider.CONTENT_URI_TOKEN, values);
    }

    public static boolean hasBtcAccount(){
        Cursor cursor = null;
        try{
            //1.query from db
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI, new String[]{DbUtils.DbColumns.ADDRESS},
                    COINTYPE_SELECTION, COINTYPE_SELECTION_BTC, null);
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

    public static int updateHasBackup(Uri uri){
        ContentValues values = new ContentValues(1);
        values.put(DbColumns.HAS_BACKUP, AppUtils.HAS_BACKUP);
        return XWalletApplication.getApplication().getApplicationContext().getContentResolver().update(uri, values,
                null, null);
    }

    public static int updateAccountBalance(String accountAddress, String balance){
        ContentValues values = new ContentValues(1);
        values.put(DbColumns.BALANCE, balance);
        return XWalletApplication.getApplication().getApplicationContext().getContentResolver().update(XWalletProvider.CONTENT_URI, values,
                ADDRESS_SELECTION, new String[]{accountAddress});
    }

    public static Cursor queryAccountToken(String accountAddress){
        return XWalletApplication.getApplication().getContentResolver().query(XWalletProvider.CONTENT_URI_TOKEN,
                new String[]{DbUtils.TokenTableColumns.NAME},
                DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " = ?",
                new String[]{accountAddress}, null);
    }

    public static boolean insertFavoriteAddressIntoDb(String address, String addressType, String addressName){
        long oldId = isFavoriteAddressExist(address);
        if (oldId > 0){
            return updateFavoriteAddress(oldId, address, addressType, addressName);
        }

        Uri uri =  XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                .insert(XWalletProvider.CONTENT_URI_ADDRESS, generateAddressContentValues(address, addressType, addressName));
        return uri != null;
    }

    public static boolean updateFavoriteAddress(long id, String address, String addressType, String addressName){
        int count = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                .update(XWalletProvider.CONTENT_URI_ADDRESS, generateAddressContentValues(address, addressType, addressName),
                        AddressTableColumns._ID + " = ?",
                        new String[]{Long.toString(id)});
        return count > 0;
    }

    public static boolean deleteFavoriteAddress(long id){
        String selection = AddressTableColumns._ID + " = ?";
        int deleteRows = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                .delete(XWalletProvider.CONTENT_URI_ADDRESS, selection, new String[]{Long.toString(id)});
        return deleteRows > 0;
    }

    private static long isFavoriteAddressExist(String address) {
        final String selection = AddressTableColumns.ADDRESS + " = ?";
        Cursor cursor = null;
        try {
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    XWalletProvider.CONTENT_URI_ADDRESS, new String[]{AddressTableColumns._ID},
                    selection, new String[]{address}, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                return cursor.getLong(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    private static ContentValues generateAddressContentValues(String address, String addressType, String addressName){
        ContentValues values = new ContentValues();
        values.put(AddressTableColumns.ADDRESS, address);
        if (!TextUtils.isEmpty(addressType)){
            values.put(AddressTableColumns.ADDRESS_TYPE, addressType);
        }

        if (!TextUtils.isEmpty(addressName)){
            values.put(AddressTableColumns.NAME, addressName);
        }
        return values;
    }
}
