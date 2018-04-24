package com.x.wallet.transaction.history;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.x.wallet.XWalletApplication;

/**
 * Created by wuliang on 18-4-24.
 */

public class TxHelper {
    private static final String LAST_TX_BLOCK_NO = "_last_tx_block_no";
    private static final String LAST_TX_PAGE_NO = "_last_tx_page_no_";
    public static void updateLastTxBlockNo(String accountAddress, String blockNo){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        preferences.edit().putString(accountAddress + LAST_TX_BLOCK_NO, blockNo).apply();
    }

    public static String getLastTxBlockNo(String accountAddress){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        return preferences.getString(accountAddress + LAST_TX_BLOCK_NO, "0");
    }

    public static void updateLastTokenTxPage(String accountAddress, String contractAddress, long page){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        preferences.edit().putLong(accountAddress + LAST_TX_PAGE_NO + contractAddress, page).apply();
    }

    public static long getLastTokenTxPage(String accountAddress, String contractAddress){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        return preferences.getLong(accountAddress + LAST_TX_PAGE_NO + contractAddress,  1);
    }
}
