package com.x.wallet;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.x.wallet.btc.BtcUtils;
import com.x.wallet.transaction.balance.BalanceConversionUtils;
import com.x.wallet.transaction.balance.BalanceLoaderManager;
import com.x.wallet.transaction.history.HistoryLoaderManager;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;

import net.bither.bitherj.crypto.mnemonic.MnemonicHelper;

/**
 * Created by wuliang on 18-3-15.
 */

public class XWalletApplication extends Application implements Application.ActivityLifecycleCallbacks{
    private static XWalletApplication mXWalletApplication = null;
    private BalanceLoaderManager mBalanceLoaderManager;
    private HistoryLoaderManager mHistoryLoaderManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mXWalletApplication = this;
        mBalanceLoaderManager = new BalanceLoaderManager(getApplicationContext());
        mHistoryLoaderManager = new HistoryLoaderManager(getApplicationContext());
        initApp();
        BtcUtils.init();
        registerActivityLifecycleCallbacks(this);
    }

    private void initApp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AppMnemonicHelper.init(MnemonicHelper.MNEMONICTYPE.MNEMONICTYPE_EN);
                    UsdToCnyHelper.init();
                    BalanceConversionUtils.init();
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

    public HistoryLoaderManager getHistoryLoaderManager() {
        return mHistoryLoaderManager;
    }

    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
        if (started <= stopped){
            Log.i("@@@@", "leave to bg.");
            AppUtils.setBackground(true);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
