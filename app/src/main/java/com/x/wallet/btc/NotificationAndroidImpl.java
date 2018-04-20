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

import android.content.Intent;
import android.util.Log;

import com.x.wallet.R;
import com.x.wallet.XWalletApplication;

import net.bither.bitherj.AbstractApp;
import net.bither.bitherj.NotificationService;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.utils.Utils;


public class NotificationAndroidImpl implements NotificationService {

    public static final String ACTION_SYNC_FROM_SPV_FINISHED = "net.bither.bitherj.SPVFinishedNotification";
    public static final String ACTION_SYNC_LAST_BLOCK_CHANGE = "net.bither.bitherj.LastBlockChangedNotification";
    public static final String ACTION_ADDRESS_BALANCE = "net.bither.bitherj.balance";
    public static final String MESSAGE_DELTA_BALANCE = "delta_balance";
    public static final String MESSAGE_ADDRESS = "address";
    public static final String MESSAGE_TX = "tx";
    public static final String MESSAGE_TX_NOTIFICATION_TYPE = "tx_notification_type";

    public static final String ACTION_SYNC_BLOCK_AND_WALLET_STATE = R.class.getPackage().getName
            () + ".sync_block_wallet";
    public static final String ACTION_PROGRESS_INFO = "progress_info";

    @Override
    public void sendBroadcastSyncSPVFinished(boolean isFinished) {
        if (isFinished) {
            AbstractApp.bitherjSetting.setBitherjDoneSyncFromSpv(isFinished);
            final Intent broadcast = new Intent(ACTION_SYNC_FROM_SPV_FINISHED);
            XWalletApplication.getApplication().sendStickyBroadcast(broadcast);
        }
    }

    @Override
    public void sendBroadcastGetSpvBlockComplete(boolean isComplete) {
        BroadcastUtil.sendBroadcastGetSpvBlockComplete(isComplete);
    }

    @Override
    public void removeBroadcastSyncSPVFinished() {
        XWalletApplication.getApplication().removeStickyBroadcast(new Intent(
                ACTION_SYNC_FROM_SPV_FINISHED));
    }

    @Override
    public void sendLastBlockChange() {
        Intent broadcast = new Intent(ACTION_SYNC_LAST_BLOCK_CHANGE);
        XWalletApplication.getApplication().sendBroadcast(broadcast);
    }

    @Override
    public void notificatTx(String address, Tx tx, Tx.TxNotificationType txNotificationType, long deltaBalance) {
        final Intent broadcast = new Intent(ACTION_ADDRESS_BALANCE);
        broadcast.putExtra(MESSAGE_ADDRESS, address);
        broadcast.putExtra(MESSAGE_DELTA_BALANCE, deltaBalance);
        if (tx != null) {
            broadcast.putExtra(MESSAGE_TX, tx.getTxHash());
        }
        if(txNotificationType != null){
            broadcast.putExtra(MESSAGE_TX_NOTIFICATION_TYPE, txNotificationType.getValue());
        }
        XWalletApplication.getApplication().sendBroadcast(broadcast);
        Log.d("","address " + address
                + " balance updated " + deltaBalance
                + (tx != null ? " tx " + Utils.hashToString(tx.getTxHash()) : "")
                + " type:" + (txNotificationType != null ? txNotificationType.getValue() : ""));

    }

    @Override
    public void sendBroadcastProgressState(double value) {
        final Intent broadcast = new Intent(ACTION_SYNC_BLOCK_AND_WALLET_STATE);
        broadcast.putExtra(ACTION_PROGRESS_INFO, value);
        XWalletApplication.getApplication().sendBroadcast(broadcast);
    }

    @Override
    public void removeProgressState() {
        XWalletApplication.getApplication().removeStickyBroadcast(new Intent
                (ACTION_SYNC_BLOCK_AND_WALLET_STATE));

    }
}
