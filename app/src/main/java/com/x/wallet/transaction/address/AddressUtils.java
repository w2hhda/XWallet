package com.x.wallet.transaction.address;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.AccountData;
import com.x.wallet.lib.common.CoinAddressHelper;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.lib.eth.api.EthAccountCreateHelper;

import java.util.Arrays;
import java.util.List;

/**
 * Created by wuliang on 18-3-14.
 */

public class AddressUtils {
    public static Uri createAddress(int coinType, String password, String accountName){

        Log.i(AppUtils.APP_TAG, "AddressUtils createAddress coinType = " + coinType + ", password = " + password);
        AccountData accountData = CoinAddressHelper.createAddress(coinType, password);
        if(accountData != null){
            fillAccountData(accountData, coinType, accountName);
            Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, DbUtils.createContentValues(accountData));
            Log.i(AppUtils.APP_TAG, "AddressUtils createAddress uri = " + uri);
            Log.i(AppUtils.APP_TAG, "AddressUtils createAddress accountData = " + accountData);
            return uri;
        } else {
            Log.i(AppUtils.APP_TAG, "AddressUtils createAddress accountData is null");

        }
        return null;
    }

    public static AccountData importAddressThroughMnemonic(int coinType, String password, String accountName, int mnemonicType, String mnemonic) {
        if(TextUtils.isEmpty(mnemonic)){
            Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughMnemonic mnemonic is null!");
            return null;
        }
        String[] mnemonicShuzu = mnemonic.split(" ");
        if(mnemonicShuzu == null){
            Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughMnemonic mnemonicShuzu is null!");
            return null;
        }
        Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughMnemonic mnemonicShuzu.length = " + mnemonicShuzu.length);
        List<String> words = Arrays.asList(mnemonicShuzu);
        AccountData accountData = CoinAddressHelper.importAddressThroughMnemonic(words, coinType, password);
        if(accountData != null){
            fillAccountData(accountData, coinType, accountName);
            //Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, DbUtils.createContentValues(accountData));
            ///Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughMnemonic uri = " + uri);
            Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughMnemonic accountData = " + accountData);
            return accountData;
        } else {
            Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughMnemonic accountData is null");
        }
        return null;
    }

    public static AccountData importAddressThroughKey(int coinType, String password, String accountName, String key) {
        if (TextUtils.isEmpty(key)){
            Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughKey key is null!");
            return null;
        }
        AccountData accountData = CoinAddressHelper.importAddressThroughKey(coinType, password, key);
        if (accountData != null) {
            fillAccountData(accountData, coinType, accountName);
            //Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, DbUtils.createContentValues(accountData));
            //Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughKey uri = " + uri);
            Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughKey accountData = " + accountData);
            return accountData;
        } else {
            Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughKey accountData is null");
        }
        return null;
    }

    public static AccountData importAddressThroughKeyStore(int coinType, String password, String accountName, String keyStore, String keyStorePassword) {
        if (TextUtils.isEmpty(keyStore) || coinType != LibUtils.COINTYPE.COIN_ETH){
            Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughKeyStore keyStore is null!");
            return null;
        }

        AccountData accountData = EthAccountCreateHelper.importFromKeyStore(keyStore, keyStorePassword, password);
        if (accountData != null){
            fillAccountData(accountData, coinType, accountName);
           //Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, DbUtils.createContentValues(accountData));
            //Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughKeyStore uri = " + uri);
            Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughKeyStore accountData = " + accountData);
            return accountData;
        } else {
            Log.i(AppUtils.APP_TAG, "AddressUtils importAddressThroughKeyStore accountData is null");
        }
        return null;
    }

    private static  void fillAccountData(AccountData accountData, int coinType, String accountName){
        accountData.setCoinName(AppUtils.COIN_ARRAY[coinType]);
        accountData.setAccountName(accountName);
        accountData.setCoinType(coinType);
    }
}