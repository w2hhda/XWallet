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
    private NetworkUtil() {

    }

    public enum NetworkType {
        Wifi, Mobile, NoConnect,
    }

    public static boolean BluetoothIsConnected() {
        /*try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            int state = adapter.getState();
            return state == BluetoothAdapter.STATE_ON;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }*/
        return false;
    }

    public static boolean isConnected() {
        // BluetoothDevice.ACTION_ACL_CONNECTED;
        // The base Context in the ContextWrapper has not been set yet, which is
        // causing the NullPointerException
        try {
            ConnectivityManager ConnectivityManager = (android.net.ConnectivityManager) XWalletApplication.getApplication().getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netinfo = ConnectivityManager.getActiveNetworkInfo();
            if (netinfo == null) {
                return false;
            } else {
                return netinfo.isAvailable();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * judge whether the network connection
     */
    public static NetworkType isConnectedType() {
        try {
            ConnectivityManager mConnectivity = (ConnectivityManager) XWalletApplication.getApplication().getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            for (NetworkInfo networkInfo : mConnectivity.getAllNetworkInfo()) {
                //LogUtil.d("network",networkInfo.getTypeName()+":"+networkInfo.getType()+",
                // "+networkInfo.isConnected());
                if (networkInfo.isConnectedOrConnecting()) {
                    return getNetworkType(networkInfo);
                }
            }
            return NetworkType.NoConnect;
        } catch (Exception e) {
            Log.w("Exception", e.getMessage() + "\n" + e.getStackTrace());
            return NetworkType.NoConnect;
        }

    }

    //TODO Determine the unknown network
    private static NetworkType getNetworkType(NetworkInfo info) {
        if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() ==
                ConnectivityManager.TYPE_ETHERNET) {
            return NetworkType.Wifi;
        } else {
            return NetworkType.Mobile;
        }
    }

}
