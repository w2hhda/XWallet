package com.x.wallet.lib.common;

import com.x.wallet.lib.eth.api.EthAccountCreateHelper;
import net.bither.bitherj.core.BtcCreateAddressHelper;
import java.security.SecureRandom;
import java.util.List;

/**
 * Created by wuliang on 18-3-21.
 */

public class CoinAddressHelper {
    public static AccountData createAddress(int coinType, String password) {
        switch (coinType) {
            case LibUtils.COINTYPE.COIN_BTC:
                return BtcCreateAddressHelper.createAddressFromRandom(new SecureRandom(), password);
            case LibUtils.COINTYPE.COIN_ETH:
                return EthAccountCreateHelper.createAccount(new SecureRandom(), password);
        }
        return null;
    }

    public static AccountData importAddressThroughMnemonic(List<String> words, int coinType, String password){
        if (coinType == LibUtils.COINTYPE.COIN_BTC) {
            return  BtcCreateAddressHelper.createAddressFromImportMnemonic(words, password);
        }else {
            return EthAccountCreateHelper.importFromMnemonic(words, password);
        }
    }

    public static AccountData importAddressThroughKey(int coinType, String password, String key) {
        switch (coinType){
            case LibUtils.COINTYPE.COIN_BTC:
                return BtcCreateAddressHelper.createAddressFromImportKey(key, password);
            case LibUtils.COINTYPE.COIN_ETH:
                return EthAccountCreateHelper.importFromPrivateKey(key, password);
        }
        return null;
    }
}
