package com.x.wallet.transaction.address;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.AccountData;
import com.x.wallet.lib.btc.BtcAddressHelper;
import com.x.wallet.lib.eth.api.EthAccountHelper;
import com.x.wallet.lib.eth.data.EthAccountData;

import java.util.Arrays;
import java.util.List;

/**
 * Created by wuliang on 18-3-14.
 */

public class AddressUtils {
    public static Uri createAddress(int coinType, String password, String accountName){
        Log.i("test3", "AddressUtils createAddress coinType = " + coinType + ", password = " + password);
        switch (coinType){
            case AppUtils.COINTYPE.COIN_BTC:
                AccountData accountData = BtcAddressHelper.create(password);
                if(accountData != null){
                    accountData.setCoinName(AppUtils.COIN_ARRAY[AppUtils.COINTYPE.COIN_BTC]);
                    accountData.setAccountName(accountName);
                    Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, AppUtils.createContentValues(accountData));
                    Log.i("test3", "AddressUtils createAddress uri = " + uri);
                    Log.i("test3", "AddressUtils createAddress accountData = " + accountData);
                    return uri;
                } else {
                    Log.i("test3", "AddressUtils createAddress accountData is null");
                }
                break;
            case AppUtils.COINTYPE.COIN_ETH:
                EthAccountData ethAccountData = EthAccountHelper.create(password);
                if (ethAccountData != null){
                    ethAccountData.setCoinName(AppUtils.COIN_ARRAY[AppUtils.COINTYPE.COIN_ETH]);
                    ethAccountData.setCoinName(accountName);
                    Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, AppUtils.createEthContentValues(ethAccountData));
                    return uri;
                }else {
                    Log.i("@@@@","create eth account fail");
                }


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
        AccountData accountData = BtcAddressHelper.createAddressFromImportMnemonic(words, password);
        if(accountData != null){
            accountData.setCoinName(AppUtils.COIN_ARRAY[AppUtils.COINTYPE.COIN_BTC]);
            accountData.setAccountName(accountName);
            Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, AppUtils.createContentValues(accountData));
            Log.i("test3", "AddressUtils importAddressThroughMnemonic uri = " + uri);
            Log.i("test3", "AddressUtils importAddressThroughMnemonic accountData = " + accountData);
            return uri;
        } else {
            Log.i("test3", "AddressUtils importAddressThroughMnemonic accountData is null");
        }
        return null;
    }

    public static Uri importAddressThroughKey(int coinType, String password, String accountName, String key) {
        return null;
    }

    public static Uri importAddressThroughKeyStore(int coinType, String password, String accountName) {
        return null;
    }
}