package com.x.wallet.btc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.bither.bitherj.AbstractApp;
import net.bither.bitherj.core.Block;
import net.bither.bitherj.core.BlockChain;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TickReceiver extends BroadcastReceiver {

    private static final int MIN_COLLECT_HISTORY = 2;
    private static final int IDLE_BLOCK_TIMEOUT_MIN = 2;
    private static final int IDLE_TRANSACTION_TIMEOUT_MIN = 9;
    private static final int MAX_HISTORY_SIZE = Math.max(
            IDLE_TRANSACTION_TIMEOUT_MIN, IDLE_BLOCK_TIMEOUT_MIN);

    private BlockchainService blockchainService;

    private AtomicInteger transactionsReceived = new AtomicInteger();

    private int lastChainHeight = 0;
    private final List<ActivityHistoryEntry> activityHistory = new
            LinkedList<ActivityHistoryEntry>();

    public TickReceiver(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;

    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Block block = BlockChain.getInstance().getLastBlock();
        int chainHeight = 0;
        if (block != null) {
            chainHeight = block.getBlockNo();
        }
        Log.i("TickReceiver", "TickReceiver onReceive chainHeight = " + chainHeight + ", lastChainHeight = " + lastChainHeight);
        if (lastChainHeight > 0) {
            final int numBlocksDownloaded = chainHeight
                    - lastChainHeight;
            final int numTransactionsReceived = transactionsReceived
                    .getAndSet(0);

            // push history
            activityHistory.add(0, new ActivityHistoryEntry(
                    numTransactionsReceived, numBlocksDownloaded));

            // trim
            while (activityHistory.size() > MAX_HISTORY_SIZE) {
                activityHistory.remove(activityHistory.size() - 1);
            }

            // print
            final StringBuilder builder = new StringBuilder();
            for (final ActivityHistoryEntry entry : activityHistory) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(entry);
            }
            Log.i("TickReceiver", "History of transactions/blocks: " + builder);

            // determine if block and transaction activity is idling
            boolean isIdle = false;
            if (activityHistory.size() >= MIN_COLLECT_HISTORY) {
                isIdle = true;
                for (int i = 0;
                     i < activityHistory.size();
                     i++) {
                    final ActivityHistoryEntry entry = activityHistory
                            .get(i);
                    final boolean blocksActive = entry.numBlocksDownloaded > 0
                            && i <= IDLE_BLOCK_TIMEOUT_MIN;
                    final boolean transactionsActive = entry.numTransactionsReceived > 0
                            && i <= IDLE_TRANSACTION_TIMEOUT_MIN;

                    if (blocksActive || transactionsActive) {
                        isIdle = false;
                        break;
                    }
                }
            }

            // if idling, shutdown service
            if (isIdle && !AbstractApp.bitherjSetting.isApplicationRunInForeground()) {
                Log.i("TickReceiver", "idling detected, stopping service");
                this.blockchainService.stopSelf();
            }
        }

        lastChainHeight = chainHeight;
    }

    public void setTransactionsReceived() {
        transactionsReceived.incrementAndGet();
    }
}
