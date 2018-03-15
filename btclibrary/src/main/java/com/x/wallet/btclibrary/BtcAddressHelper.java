package com.x.wallet.btclibrary;

import android.util.Log;

import net.bither.bitherj.core.HDAccount;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;

import java.security.SecureRandom;

/**
 * Created by wuliang on 18-3-14.
 */

public class BtcAddressHelper {
    public static AccountData create(String password){
        Log.i("test3", "BtcAddressHelper create password = " + password);
        try {
            HDAccount hdAccount = new HDAccount(new SecureRandom(), password);
            return hdAccount.getAccountData();
        } catch (MnemonicException.MnemonicLengthException e) {
            Log.e("test3", "BtcAddressHelper create", e);
        }
        Log.i("test3", "BtcAddressHelper create return null");
        return null;
    }
}
