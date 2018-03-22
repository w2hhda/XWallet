package com.x.wallet.transaction.address;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.AccountData;
import com.x.wallet.lib.btc.BtcAddressHelper;
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
            accountData.setCoinName(AppUtils.COIN_ARRAY[coinType]);
            accountData.setAccountName(accountName);
            accountData.setCoinType(coinType);
            Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, DbUtils.createContentValues(accountData));
            Log.i(AppUtils.APP_TAG, "AddressUtils createAddress uri = " + uri);
            Log.i(AppUtils.APP_TAG, "AddressUtils createAddress accountData = " + accountData);
            return uri;
        } else {
            Log.i(AppUtils.APP_TAG, "AddressUtils createAddress accountData is null");

        }
        return null;
    }

    public static Uri importAddressThroughMnemonic(int coinType, String password, String accountName, int mnemonicType, String mnemonic) {
        if(TextUtils.isEmpty(mnemonic)){
            Log.i("test3", "AddressUtils importAddressThroughMnemonic mnemonic is null!");
            return null;
        }
        String[] mnemonicShuzu = mnemonic.split(" ");
        if(mnemonicShuzu == null){
            Log.i("test3", "AddressUtils importAddressThroughMnemonic mnemonicShuzu is null!");
            return null;
        }
        Log.i("test3", "AddressUtils importAddressThroughMnemonic mnemonicShuzu.length = " + mnemonicShuzu.length);
        List<String> words = Arrays.asList(mnemonicShuzu);
        AccountData accountData;
        if (coinType == LibUtils.COINTYPE.COIN_BTC) {
            accountData = BtcAddressHelper.createAddressFromImportMnemonic(words, password);
        }else {
            accountData = EthAccountCreateHelper.importFromMnemonic(words, password);
        }
        if(accountData != null){
            accountData.setCoinName(coinType + "");
            accountData.setAccountName(accountName);
            Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, DbUtils.createContentValues(accountData));
            Log.i("test3", "AddressUtils importAddressThroughMnemonic uri = " + uri);
            Log.i("test3", "AddressUtils importAddressThroughMnemonic accountData = " + accountData);
            return uri;
        } else {
            Log.i("test3", "AddressUtils importAddressThroughMnemonic accountData is null");
        }
        return null;
    }

    public static Uri importAddressThroughKey(int coinType, String password, String accountName, String key) {
        if (TextUtils.isEmpty(key)){
            return null;
        }
        if (coinType == LibUtils.COINTYPE.COIN_ETH) {
            AccountData accountData = EthAccountCreateHelper.importFromPrivateKey(key, password);
            if (accountData != null) {
                accountData.setCoinName(AppUtils.COIN_ARRAY[coinType]);
                accountData.setAccountName(accountName);
                Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, DbUtils.createContentValues(accountData));
                return uri;
            }
        }else {

        }


        return null;
    }

    public static Uri importAddressThroughKeyStore(int coinType, String password, String accountName, String keyStore) {
        if (TextUtils.isEmpty(keyStore) || coinType != LibUtils.COINTYPE.COIN_ETH){
            return null;
        }

        AccountData accountData = EthAccountCreateHelper.importFromKeyStore(keyStore, password);
        if (accountData != null){
            accountData.setCoinName(AppUtils.COIN_ARRAY[coinType]);
            accountData.setAccountName(accountName);
            Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, DbUtils.createContentValues(accountData));
            return uri;
        }else {
        }
        return null;
    }
}