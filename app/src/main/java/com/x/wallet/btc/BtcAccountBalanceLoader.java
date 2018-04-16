package com.x.wallet.btc;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.x.wallet.lib.btc.BtcLibHelper;

/**
 * Created by wuliang on 18-3-30.
 */

public class BtcAccountBalanceLoader extends AsyncTaskLoader<Long> {
    private String mAddress;

    public BtcAccountBalanceLoader(Context context, String address) {
        super(context);
        mAddress = address;
    }

    @Override
    public Long loadInBackground() {
        return BtcLibHelper.updateBalance(mAddress);
    }

}
