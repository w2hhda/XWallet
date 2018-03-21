package com.x.wallet.lib.btc;

import android.util.Log;

import com.x.wallet.lib.common.AccountData;

import net.bither.bitherj.core.BtcCreateAddressHelper;

import java.util.List;

/**
 * Created by wuliang on 18-3-14.
 */

public class BtcAddressHelper {

    public static AccountData createAddressFromImportMnemonic(List<String> words, String password){
        Log.i("test3", "BtcAddressHelper createAddressFromImportMnemonic password = " + password);
        return BtcCreateAddressHelper.createAddressFromImportMnemonic(words, password);
    }
}
