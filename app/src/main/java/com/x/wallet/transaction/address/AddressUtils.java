package com.x.wallet.transaction.address;

import android.net.Uri;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.btclibrary.AccountData;
import com.x.wallet.btclibrary.BtcAddressHelper;
import com.x.wallet.db.XWalletProvider;

/**
 * Created by wuliang on 18-3-14.
 */

public class AddressUtils {
    public static Uri createAddress(int coinType, String password, String accountName){
        Log.i("test3", "AddressUtils createAddress coinType = " + coinType + ", password = " + password);
        switch (coinType){
            case AppUtils.COINTYPE.COIN_BTC:
                AccountData accountData = BtcAddressHelper.create(password);
                accountData.setCoinName(AppUtils.COIN_ARRAY[AppUtils.COINTYPE.COIN_BTC]);
                accountData.setAccountName(accountName);
                if(accountData != null){
                    Uri uri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, AppUtils.createContentValues(accountData));
                    Log.i("test3", "AddressUtils createAddress uri = " + uri);
                    Log.i("test3", "AddressUtils createAddress accountData = " + accountData);
                    return uri;
                } else {
                    Log.i("test3", "AddressUtils createAddress accountData is null");
                }
                break;
        }
        return null;
    }
}