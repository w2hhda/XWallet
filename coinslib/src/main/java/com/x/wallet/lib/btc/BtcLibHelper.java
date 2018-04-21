package com.x.wallet.lib.btc;

import net.bither.bitherj.core.In;
import net.bither.bitherj.core.Out;
import net.bither.bitherj.core.OutPoint;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.utils.Utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wuliang on 18-4-16.
 */

public class BtcLibHelper {
    public static long deltaAmountFrom(String address, Tx transaction) {
        long receive = 0;
        for (Out out : transaction.getOuts()) {
            if (Utils.compareString(address, out.getOutAddress())) {
                receive += out.getOutValue();
            }
        }
        long sent = AbstractDb.txProvider.sentFromAddress(transaction.getTxHash(), address);
        return receive - sent;
    }

    public static long updateBalance(String address) {
        return AbstractDb.txProvider.getConfirmedBalanceWithAddress(address)
                + calculateUnconfirmedBalance(address);
    }

    private static long calculateUnconfirmedBalance(String address) {
        long balance = 0;

        List<Tx> txs = AbstractDb.txProvider.getUnconfirmedTxWithAddress(address);
        Collections.sort(txs);

        Set<byte[]> invalidTx = new HashSet<byte[]>();
        Set<OutPoint> spentOut = new HashSet<OutPoint>();
        Set<OutPoint> unspendOut = new HashSet<OutPoint>();

        for (int i = txs.size() - 1; i >= 0; i--) {
            Set<OutPoint> spent = new HashSet<OutPoint>();
            Tx tx = txs.get(i);

            Set<byte[]> inHashes = new HashSet<byte[]>();
            for (In in : tx.getIns()) {
                spent.add(new OutPoint(in.getPrevTxHash(), in.getPrevOutSn()));
                inHashes.add(in.getPrevTxHash());
            }

            if (tx.getBlockNo() == Tx.TX_UNCONFIRMED
                    && (Utils.isIntersects(spent, spentOut) || Utils.isIntersects(inHashes, invalidTx))) {
                invalidTx.add(tx.getTxHash());
                continue;
            }

            spentOut.addAll(spent);
            for (Out out : tx.getOuts()) {
                if (Utils.compareString(address, out.getOutAddress())) {
                    unspendOut.add(new OutPoint(tx.getTxHash(), out.getOutSn()));
                    balance += out.getOutValue();
                }
            }
            spent.clear();
            spent.addAll(unspendOut);
            spent.retainAll(spentOut);
            for (OutPoint o : spent) {
                Tx tx1 = AbstractDb.txProvider.getTxDetailByTxHash(o.getTxHash());
                unspendOut.remove(o);
                for (Out out : tx1.getOuts()) {
                    if (out.getOutSn() == o.getOutSn()) {
                        balance -= out.getOutValue();
                    }
                }
            }
        }
        return balance;
    }

    public static List<Tx> getTxs(String address, int page) {
        List<Tx> txs = AbstractDb.txProvider.getTxAndDetailByAddress(address, page);
        return txs;
    }

    public static int getTxsCount(String address) {
        return AbstractDb.txProvider.getTxsCount(address);
    }
}
