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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.x.wallet.R;
import com.x.wallet.lib.btc.CustomeTransactionsUtil;

import net.bither.bitherj.AbstractApp;
import net.bither.bitherj.core.Block;
import net.bither.bitherj.core.PeerManager;
import net.bither.bitherj.exception.BlockStoreException;
import net.bither.bitherj.utils.BlockUtil;

import java.util.ArrayList;
import java.util.List;

public class BlockchainService extends android.app.Service {

    public static final String ACTION_BEGIN_DOWLOAD_SPV_BLOCK = R.class
            .getPackage().getName() + ".dowload_block_api_begin";
    private WakeLock wakeLock;
    private SPVFinishedReceiver spvFinishedReceiver = null;
    private TickReceiver tickReceiver = null;
    private TxReceiver txReceiver = null;

    private boolean connectivityReceivered = false;
    private boolean peerCanNotRun = false;

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.i("test34", "BlockchainService onCreate");
        HandlerThread thread = new HandlerThread("BlockchainService");
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        final String lockName = getPackageName() + " blockchain sync";
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, lockName);

        tickReceiver = new TickReceiver(BlockchainService.this);
        txReceiver = new TxReceiver(BlockchainService.this, tickReceiver);
        receiverConnectivity();
        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        registerReceiver(txReceiver, new IntentFilter(NotificationAndroidImpl.ACTION_ADDRESS_BALANCE));

        BroadcastUtil.sendBroadcastStartPeer();
    }

    private void receiverConnectivity() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        intentFilter.addAction(BroadcastUtil.ACTION_START_PEER_MANAGER);
        registerReceiver(connectivityReceiver, intentFilter);
        connectivityReceivered = true;

    }

    @Override
    public void onDestroy() {
        //Log.i("test34", "BlockchainService onDestroy");
        PeerManager.instance().stop();
        PeerManager.instance().onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
        if (connectivityReceivered) {
            unregisterReceiver(connectivityReceiver);
            connectivityReceivered = false;
        }
        if (tickReceiver != null) {
            unregisterReceiver(tickReceiver);
        }
        if (txReceiver != null) {
            unregisterReceiver(txReceiver);
        }
        BroadcastUtil.removeMarketState();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.w("BlockchainService","BlockchainService onLowMemory, stopping service");
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        //Log.i("test34", "BlockchainService onStartCommand intent = " + intent);
        if (intent == null) {
            return START_NOT_STICKY;
        }
        final String action = intent.getAction();
        if (ACTION_BEGIN_DOWLOAD_SPV_BLOCK.equals(action)) {
            new Thread(new DownloadSpvRunnable()).start();
        } else if(intent.hasExtra(BtcUtils.BLOCKCHAIN_SERVICE_ACTION)){
            Message msg = mServiceHandler.obtainMessage(intent.getIntExtra(BtcUtils.BLOCKCHAIN_SERVICE_ACTION, BtcUtils.BLOCKCHAIN_SERVICE_ACTION_START));
            msg.arg1 = startId;
            msg.obj = intent;
            mServiceHandler.sendMessage(msg);
        }
        return START_NOT_STICKY;
    }

    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        private boolean hasConnectivity;
        private boolean hasStorage = true;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        onReceive(intent);
                    } catch (BlockStoreException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        }

        private void onReceive(final Intent intent) throws BlockStoreException {
            final String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                hasConnectivity = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                //Log.i("test34", "BlockchainService onReceive network is " + (hasConnectivity ? "up" : "down"));
                check();
            } else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action)) {
                hasStorage = false;
                //Log.i("test34", "BlockchainService onReceive device storage low");
                check();
            } else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action)) {
                hasStorage = true;
                //Log.i("test34", "BlockchainService onReceive device storage ok");
                check();
            } else if (BroadcastUtil.ACTION_START_PEER_MANAGER.equals(action)) {
                hasStorage = true;
                //Log.i("test34", "BlockchainService onReceive ACTION_START_PEER_MANAGER");
                check();
            }
        }

        @SuppressLint("Wakelock")
        private void check() throws BlockStoreException {
            final boolean hasEverything = hasConnectivity && hasStorage;
            NetworkUtil.NetworkType networkType = NetworkUtil.isConnectedType();
            boolean networkIsAvailadble = (networkType == NetworkUtil.NetworkType.Wifi);
            //Log.i("test34", "BlockchainService check networkType = " + networkType);
            if (networkIsAvailadble && hasEverything) {
                callWekelock();
                if (!PeerManager.instance().isRunning()) {
                    startPeer();
                }
            } else {
                PeerManager.instance().stop();
            }
        }
    };

    public void stopAndUnregister() {
        Log.i("testPeer", "BlockchainService stopAndUnregister");
        peerCanNotRun = true;
        if (connectivityReceivered) {
            unregisterReceiver(connectivityReceiver);
            connectivityReceivered = false;
        }
        PeerManager.instance().stop();
    }

    public void startAndRegister() {
        Log.i("testPeer", "BlockchainService startAndRegister");
        peerCanNotRun = false;
        receiverConnectivity();
        new Thread(new Runnable() {
            @Override
            public void run() {
                startPeer();
            }
        }).start();

    }

    private void callWekelock() {
        if ((wakeLock != null) && // we have a WakeLock
                !wakeLock.isHeld()) { // but we don't hold it
            try {
                // WakeLock.acquireLocked(PowerManager.java:329) sdk16 nullpoint
                wakeLock.acquire();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private boolean spvFinishedReceivered = false;

    private synchronized void startPeer() {
        //Log.i("test34", "BlockchainService startPeer 0");
        try {
            if (peerCanNotRun) {
                return;
            }
            //Log.i("test34", "BlockchainService startPeer 1");
            if (!AppSharedPreference.getInstance().getDownloadSpvFinish()) {
                //Log.i("test34", "BlockchainService startPeer 2");
                Block block = BlockUtil.dowloadSpvBlock();
                if (block == null) {
                    return;
                }
            }
            //Log.i("test34", "BlockchainService startPeer 3");
            if (!AppSharedPreference.getInstance().getBitherjDoneSyncFromSpv()) {
                if (!PeerManager.instance().isConnected()) {
                    //Log.i("test34", "BlockchainService startPeer 4");
                    PeerManager.instance().start();
                    if (!spvFinishedReceivered) {
                        final IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(NotificationAndroidImpl.ACTION_SYNC_FROM_SPV_FINISHED);
                        spvFinishedReceiver = new SPVFinishedReceiver();
                        registerReceiver(spvFinishedReceiver, intentFilter);
                        spvFinishedReceivered = true;
                    }
                }
            } else {
                //Log.i("test34", "BlockchainService startPeer 5");
                handleAddressSync(true);
                startPeerManager();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startPeerManager() {
        //Log.i("test34", "BlockchainService startPeerManager");
        if (handleAddressSync(false)
                && AppSharedPreference.getInstance().getBitherjDoneSyncFromSpv()
                && AppSharedPreference.getInstance().getDownloadSpvFinish()) {
            //Log.i("test34", "BlockchainService startPeerManager 1");
            NetworkUtil.NetworkType networkType = NetworkUtil.isConnectedType();
            boolean networkIsAvailadble =  (networkType == NetworkUtil.NetworkType.Wifi);
            if (networkIsAvailadble && !PeerManager.instance().isConnected()) {
                //Log.i("test34", "BlockchainService startPeerManager 2");
                PeerManager.instance().start();
            }
        }
    }

    public class SPVFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.i("test34", "SPVFinishedReceiver onReceive intent= " + intent);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        handleAddressSync(true);
                        startPeerManager();
                        AbstractApp.notificationService.removeBroadcastSyncSPVFinished();
                        if (spvFinishedReceiver != null && spvFinishedReceivered) {
                            unregisterReceiver(spvFinishedReceiver);
                            spvFinishedReceivered = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        }
    }

    private static boolean handleAddressSync(boolean isNeedGet){
        Cursor cursor = null;
        try{
            cursor = BtcDbHelper.queryAllNotSyncedAddress();
            if(cursor != null && cursor.getCount() > 0){
                if(isNeedGet){
                    List<String> addressList = new ArrayList<>();
                    while (cursor.moveToNext()){
                        addressList.add(cursor.getString(1));
                    }
                    //Log.i("test34", "BlockchainService handleAddressSync size = " + addressList.size());
                    if(addressList.size() > 0){
                        CustomeTransactionsUtil.getMyTxFromBither(addressList);
                    }
                }
                return false;
            }
        } catch (Exception e){

        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return true;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            //Log.i("test34", "BlockchainService handleMessage msg.what = " + decodeMessage(msg));
            switch (msg.what) {
                case BtcUtils.BLOCKCHAIN_SERVICE_ACTION_START_PEER:
                        startAndRegister();
                    break;
                case BtcUtils.BLOCKCHAIN_SERVICE_ACTION_STOP_PEER:
                        stopAndUnregister();
                    break;
            }
        }

        private String decodeMessage(Message msg) {
            switch (msg.what){
                case BtcUtils.BLOCKCHAIN_SERVICE_ACTION_START_PEER:
                    return "BLOCKCHAIN_SERVICE_ACTION_START_PEER";
                case BtcUtils.BLOCKCHAIN_SERVICE_ACTION_STOP_PEER:
                    return "BLOCKCHAIN_SERVICE_ACTION_STOP_PEER";
            }
            return String.valueOf(msg.what);
        }
    }
}
