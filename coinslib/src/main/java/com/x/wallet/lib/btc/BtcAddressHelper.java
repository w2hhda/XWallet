package com.x.wallet.lib.btc;

import android.util.Log;

import com.x.wallet.lib.common.AccountData;

import net.bither.bitherj.core.BtcCreateAddressHelper;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;

import java.security.SecureRandom;
import java.util.List;

/**
 * Created by wuliang on 18-3-14.
 */

public class BtcAddressHelper {
    public static AccountData create(String password){
        Log.i("test3", "BtcAddressHelper create password = " + password);
        try {
            return BtcCreateAddressHelper.createAddressFromRandom(new SecureRandom(), password);
        } catch (MnemonicException.MnemonicLengthException e) {
            Log.e("test3", "BtcAddressHelper create", e);
        }
        Log.i("test3", "BtcAddressHelper create return null");
        return null;
    }

    public static AccountData createAddressFromImportMnemonic(List<String> words, String password){
        Log.i("test3", "BtcAddressHelper createAddressFromImportMnemonic password = " + password);
        return BtcCreateAddressHelper.createAddressFromImportMnemonic(words, password);
    }
}
