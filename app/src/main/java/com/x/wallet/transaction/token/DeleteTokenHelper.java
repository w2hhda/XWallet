package com.x.wallet.transaction.token;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.x.wallet.XWalletApplication;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by wuliang on 18-4-23.
 */

public class DeleteTokenHelper {
    public static void addTokenToDeletedSet(String accountAddress, String tokenName){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        Set<String> deletedTokenSet;
        deletedTokenSet = preferences.getStringSet(accountAddress, null);
        if(deletedTokenSet == null){
            deletedTokenSet = new HashSet<>();
            deletedTokenSet.add(tokenName);
            preferences.edit().putStringSet(accountAddress, deletedTokenSet).apply();
        } else {
            if(!deletedTokenSet.contains(tokenName)){
                deletedTokenSet.add(tokenName);
                preferences.edit().putStringSet(accountAddress, deletedTokenSet).apply();
            }
        }
    }

    public static void removeTokenFromDeletedSet(String accountAddress, String tokenName){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        Set<String> deletedTokenSet = preferences.getStringSet(accountAddress, null);
        if(deletedTokenSet != null && deletedTokenSet.contains(tokenName)){
            deletedTokenSet.remove(tokenName);
            preferences.edit().putStringSet(accountAddress, deletedTokenSet).apply();
        }
    }

    public static boolean isDeletedToken(String accountAddress, String tokenName){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        Set<String> deletedTokenSet = preferences.getStringSet(accountAddress, null);
        return deletedTokenSet != null && deletedTokenSet.contains(tokenName);
    }

    public static void removeSharePref(String accountAddress){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        preferences.edit().remove(accountAddress).apply();
    }
}
