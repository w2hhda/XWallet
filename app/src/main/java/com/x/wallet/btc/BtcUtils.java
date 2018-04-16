package com.x.wallet.btc;

import android.content.Intent;

import com.x.wallet.XWalletApplication;
import com.x.wallet.lib.common.LibUtils;

/**
 * Created by wuliang on 18-4-13.
 */

public class BtcUtils {
    public static final int BTC_DECIMALS_COUNT = 8;

    public static final String BLOCKCHAIN_SERVICE_ACTION = "blockchain_service_action";
    public static final int BLOCKCHAIN_SERVICE_ACTION_START = 0;
    public static final int BLOCKCHAIN_SERVICE_ACTION_STOP_PEER = 1;
    public static final int BLOCKCHAIN_SERVICE_ACTION_START_PEER = 2;

    public static void init(){
        AndroidDbImpl androidDb = new AndroidDbImpl();
        androidDb.construct();
        AndroidImplAbstractApp appAndroid = new AndroidImplAbstractApp();
        appAndroid.construct();
    }

    public static void visitBlockchainService(int action){
        Intent intent = new Intent(XWalletApplication.getApplication().getApplicationContext(), BlockchainService.class);
        intent.putExtra(BLOCKCHAIN_SERVICE_ACTION, action);
        XWalletApplication.getApplication().getApplicationContext().startService(intent);
    }

    public static void stopPeer(int coinType){
        if(coinType == LibUtils.COINTYPE.COIN_BTC){
            BtcUtils.visitBlockchainService(BtcUtils.BLOCKCHAIN_SERVICE_ACTION_STOP_PEER);
        }
    }

    public static void startPeer(int coinType){
        if(coinType == LibUtils.COINTYPE.COIN_BTC){
            BtcUtils.visitBlockchainService(BtcUtils.BLOCKCHAIN_SERVICE_ACTION_START_PEER);
        }
    }
}
