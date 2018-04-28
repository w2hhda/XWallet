/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.x.wallet.btc;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import com.x.wallet.XWalletApplication;

import net.bither.bitherj.AbstractApp;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.ISetting;
import net.bither.bitherj.NotificationService;
import net.bither.bitherj.qrcode.QRCodeUtil;

import java.io.File;
import java.util.List;

public class AndroidImplAbstractApp extends AbstractApp {

    @Override
    public ISetting initSetting() {
        return new ISetting() {
            @Override
            public BitherjSettings.AppMode getAppMode() {
                return AppSharedPreference.getInstance().getAppMode();
            }

            @Override
            public boolean getBitherjDoneSyncFromSpv() {
                return AppSharedPreference.getInstance().getBitherjDoneSyncFromSpv();
            }

            @Override
            public void setBitherjDoneSyncFromSpv(boolean isDone) {
                AppSharedPreference.getInstance().setBitherjDoneSyncFromSpv(isDone);
            }

            @Override
            public BitherjSettings.TransactionFeeMode getTransactionFeeMode() {
                return AppSharedPreference.getInstance().getTransactionFeeMode();
            }

            @Override
            public BitherjSettings.ApiConfig getApiConfig() {
                return AppSharedPreference.getInstance().getApiConfig();
            }

            @Override
            public File getPrivateDir(String dirName) {
                File file = XWalletApplication.getApplication().getDir(dirName, Context.MODE_PRIVATE);
                if (!file.exists()) {
                    file.mkdirs();
                }
                return file;
            }

            @Override
            public boolean isApplicationRunInForeground() {
                if (XWalletApplication.getApplication() == null) {
                    return false;
                }
                ActivityManager am = (ActivityManager) XWalletApplication.getApplication()
                        .getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                if (tasks != null && !tasks.isEmpty()) {
                    ComponentName topActivity = tasks.get(0).topActivity;
                    if (!topActivity.getPackageName().equals(XWalletApplication.getApplication() .getPackageName())) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public QRCodeUtil.QRQuality getQRQuality() {
                return AppSharedPreference.getInstance().getQRQuality();
            }

            @Override
            public boolean getDownloadSpvFinish() {
                return AppSharedPreference.getInstance().getDownloadSpvFinish();
            }

            @Override
            public void setDownloadSpvFinish(boolean finish) {
                AppSharedPreference.getInstance().setDownloadSpvFinish(finish);
            }
        };
    }

    @Override
    public NotificationService initNotification() {
        return new NotificationAndroidImpl();
    }
}
