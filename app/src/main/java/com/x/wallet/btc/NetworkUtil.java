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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.x.wallet.XWalletApplication;

public class NetworkUtil {

    public enum NetworkType {
        Wifi, Mobile, NoConnect,
    }

    public static NetworkType isConnectedType() {
        try {
            ConnectivityManager mConnectivity = (ConnectivityManager) XWalletApplication.getApplication().getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            for (NetworkInfo networkInfo : mConnectivity.getAllNetworkInfo()) {
                if (networkInfo.isConnectedOrConnecting()) {
                    return getNetworkType(networkInfo);
                }
            }
            return NetworkType.NoConnect;
        } catch (Exception e) {
            //Log.e("test34", "NetworkUtil isConnectedType", e);
            return NetworkType.NoConnect;
        }

    }

    private static NetworkType getNetworkType(NetworkInfo info) {
        if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == ConnectivityManager.TYPE_ETHERNET) {
            return NetworkType.Wifi;
        } else {
            return NetworkType.Mobile;
        }
    }

}
