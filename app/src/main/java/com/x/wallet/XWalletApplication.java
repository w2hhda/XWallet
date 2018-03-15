package com.x.wallet;

import android.app.Application;

import net.bither.bitherj.crypto.mnemonic.MnemonicWordList;

import java.io.IOException;

/**
 * Created by wuliang on 18-3-15.
 */

public class XWalletApplication extends Application{
    private static XWalletApplication mXWalletApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mXWalletApplication = this;
        initApp();
    }

    private void initApp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MnemonicCodeAndroid.setMnemonicCode(MnemonicWordList.English);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    synchronized public static XWalletApplication getApplication() {
        return mXWalletApplication;
    }
}
