package com.x.wallet.lib.btc;

import net.bither.bitherj.core.Tx;

/**
 * Created by wuliang on 18-4-12.
 */

public class TxBuildResult {
    public Tx mTx;
    public int mResultCode;

    public TxBuildResult(Tx tx, int resultCode) {
        mTx = tx;
        mResultCode = resultCode;
    }

    public interface ResultCode{
        int RESULT_OK = -1;
        int ERROR_UNKNOWN = 0;
        int ERROR_NOT_ENOUGH_MONEY = 1;
        int ERROR_WAIT_CONFIRM = 2;
        int ERROR_DUST_OUTPUT = 4;
        int ERROR_REACH_MAX_TX_SIZE_LIMIT = 5;
    }
}
