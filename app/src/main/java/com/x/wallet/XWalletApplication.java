package com.x.wallet;

import android.app.Application;
import android.util.Log;

import com.x.wallet.transaction.balance.BalanceLoaderManager;

import net.bither.bitherj.crypto.mnemonic.MnemonicHelper;

/**
 * Created by wuliang on 18-3-15.
 */

public class XWalletApplication extends Application{
    private static XWalletApplication mXWalletApplication = null;
    private BalanceLoaderManager mBalanceLoaderManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mXWalletApplication = this;
        mBalanceLoaderManager = new BalanceLoaderManager(getApplicationContext());
        initApp();
    }

    private void initApp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AppMnemonicHelper.init(MnemonicHelper.MNEMONICTYPE.MNEMONICTYPE_EN);
                } catch (Exception e) {
                    Log.e("XWalletApplication", "initApp has a exception!", e);
                }
            }
        }).start();
    }

    synchronized public static XWalletApplication getApplication() {
        return mXWalletApplication;
    }

    public BalanceLoaderManager getBalanceLoaderManager() {
        return mBalanceLoaderManager;
    }
}
