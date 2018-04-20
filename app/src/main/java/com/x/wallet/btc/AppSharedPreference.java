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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.x.wallet.XWalletApplication;

import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.qrcode.QRCodeUtil;

public class AppSharedPreference {
    private static final String DOWNLOAD_SPV_FINISH = "download_spv_finish";

    private static final String TRANSACTION_FEE_MODE = "transaction_fee_mode";
    private static final String BITHERJ_DONE_SYNC_FROM_SPV = "bitheri_done_sync_from_spv";

    private static final String QR_QUALITY = "qr_quality";

    private static AppSharedPreference mInstance = new AppSharedPreference();

    private SharedPreferences mPreferences;

    public static AppSharedPreference getInstance() {
        return mInstance;
    }

    private AppSharedPreference() {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
    }

    public BitherjSettings.AppMode getAppMode() {
        return BitherjSettings.AppMode.HOT;
    }

    public boolean getBitherjDoneSyncFromSpv() {
        return mPreferences.getBoolean(BITHERJ_DONE_SYNC_FROM_SPV, false);
    }

    public void setBitherjDoneSyncFromSpv(boolean isDone) {
        mPreferences.edit().putBoolean(BITHERJ_DONE_SYNC_FROM_SPV, isDone).commit();

    }

    public BitherjSettings.TransactionFeeMode getTransactionFeeMode() {
        int ordinal = this.mPreferences.getInt(TRANSACTION_FEE_MODE, -1);
        if (ordinal < BitherjSettings.TransactionFeeMode.values().length && ordinal >= 0) {
            return BitherjSettings.TransactionFeeMode.values()[ordinal];
        }
        return BitherjSettings.TransactionFeeMode.TenX;
    }

    public BitherjSettings.ApiConfig getApiConfig() {
        return BitherjSettings.ApiConfig.BLOCKCHAIN_INFO;
    }

    public boolean getDownloadSpvFinish() {
        return mPreferences.getBoolean(DOWNLOAD_SPV_FINISH, false);
    }

    public void setDownloadSpvFinish(boolean finish) {
        this.mPreferences.edit().putBoolean(DOWNLOAD_SPV_FINISH, finish).commit();
    }

    public QRCodeUtil.QRQuality getQRQuality() {
        int ordinal = this.mPreferences.getInt(QR_QUALITY, 0);
        if (ordinal < QRCodeUtil.QRQuality.values().length && ordinal >= 0) {
            return QRCodeUtil.QRQuality.values()[ordinal];
        }
        return QRCodeUtil.QRQuality.Normal;

    }


}
