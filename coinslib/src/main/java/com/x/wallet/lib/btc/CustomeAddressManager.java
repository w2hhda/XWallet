package com.x.wallet.lib.btc;

import android.util.Log;

import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.In;
import net.bither.bitherj.core.Out;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.utils.Sha256Hash;
import net.bither.bitherj.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wuliang on 18-4-13.
 */

public class CustomeAddressManager {
    public static boolean isTxRelated(HashSet<String> addressHashSet, Tx tx, List<String> inAddresses) {
        if(inAddresses != null){
            for(String address : inAddresses){
                System.out.println("testTx CustomeAddressManager isTxRelated address = " + address);
            }
        }
        for (String address : addressHashSet) {
            if (isAddressContainsTx(address, tx)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAddressContainsTx(String address, Tx tx) {
        Set<String> outAddress = new HashSet<String>();
        for (Out out : tx.getOuts()) {
            System.out.println("testTx CustomeAddressManager isAddressContainsTx address = " + out.getOutAddress());
            outAddress.add(out.getOutAddress());
        }
        if (outAddress.contains(address)) {
            return true;
        } else {
            return AbstractDb.txProvider.isAddressContainsTx(address, tx);
        }
    }

    public static boolean registerTx(HashSet<String> addressHashSet, Tx tx, Tx.TxNotificationType txNotificationType, boolean isConfirmed) {
        if (isConfirmed) {
            byte[] existTx = AbstractDb.txProvider.isIdentify(tx);
            if (existTx.length > 0) {
                AbstractDb.txProvider.remove(existTx);
            }
        } else {
            byte[] existTx = AbstractDb.txProvider.isIdentify(tx);
            if (existTx.length > 0) {
                return false;
            }
        }
        if (AbstractDb.txProvider.isTxDoubleSpendWithConfirmedTx(tx)) {
            // double spend with confirmed tx
            return false;
        }

        List<String> inAddresses = tx.getInAddresses();
        boolean isRegister = false;
        Tx compressedTx;
        if (txNotificationType != Tx.TxNotificationType.txSend) {
            compressedTx = compressTx(addressHashSet, tx, inAddresses);
        } else {
            compressedTx = tx;
        }

        HashSet<String> needNotifyAddressHashSet = new HashSet<String>();

        for (Out out : compressedTx.getOuts()) {
            String outAddress = out.getOutAddress();
            if (addressHashSet.contains(outAddress)) {
                needNotifyAddressHashSet.add(outAddress);
            }
        }

        Tx txInDb = AbstractDb.txProvider.getTxDetailByTxHash(tx.getTxHash());
        if (txInDb != null) {
            for (Out out : txInDb.getOuts()) {
                String outAddress = out.getOutAddress();
                if (needNotifyAddressHashSet.contains(outAddress)) {
                    needNotifyAddressHashSet.remove(outAddress);
                }
            }
            isRegister = true;
        } else {
            for (String address : inAddresses) {
                if (addressHashSet.contains(address)) {
                    needNotifyAddressHashSet.add(address);
                }
            }
            isRegister = needNotifyAddressHashSet.size() > 0;
        }


        if (needNotifyAddressHashSet.size() > 0) {
            AbstractDb.txProvider.add(compressedTx);
            Log.i("testTx", "CustomeAddressManager registerTx txHash = " + Utils.hashToString(tx.getTxHash()));
        }
        for (String addr : addressHashSet) {
            if (needNotifyAddressHashSet.contains(addr)) {
                //Address.notificatTx(tx, txNotificationType);
            }
        }

        return isRegister;
    }

    public static Tx compressTx(HashSet<String> addressHashSet, Tx tx, List<String> inAddresses) {
        if (tx.getOuts().size() > BitherjSettings.COMPRESS_OUT_NUM
                && !isSendFromMe(addressHashSet, tx, inAddresses)) {
            List<Out> outList = new ArrayList<Out>();
            for (Out out : tx.getOuts()) {
                String outAddress = out.getOutAddress();
                if (addressHashSet.contains(outAddress) || out.getHDAccountId() > 0) {
                    outList.add(out);
                }
            }
            tx.setOuts(outList);
        }
        return tx;
    }

    private static boolean isSendFromMe(HashSet<String> addressHashSet, Tx tx, List<String> addresses) {
        return addressHashSet.containsAll(addresses) || AbstractDb.hdAccountAddressProvider.getRelatedAddressCnt(addresses) > 0;
    }

    public static List<Tx> compressTxsForApi(List<Tx> txList, String address) {
        Map<Sha256Hash, Tx> txHashList = new HashMap<Sha256Hash, Tx>();
        for (Tx tx : txList) {
            txHashList.put(new Sha256Hash(tx.getTxHash()), tx);
        }
        for (Tx tx : txList) {
            if (!isSendFromMe(tx, txHashList, address) && tx.getOuts().size() > BitherjSettings
                    .COMPRESS_OUT_NUM) {
                List<Out> outList = new ArrayList<Out>();
                for (Out out : tx.getOuts()) {
                    if (Utils.compareString(address, out.getOutAddress())) {
                        outList.add(out);
                    }
                }
                tx.setOuts(outList);
            }
        }

        return txList;
    }

    private static boolean isSendFromMe(Tx tx, Map<Sha256Hash, Tx> txHashList, String address) {
        for (In in : tx.getIns()) {
            Sha256Hash prevTxHahs = new Sha256Hash(in.getPrevTxHash());
            if (txHashList.containsKey(prevTxHahs)) {
                Tx preTx = txHashList.get(prevTxHahs);
                for (Out out : preTx.getOuts()) {
                    if (out.getOutSn() == in.getPrevOutSn()) {
                        if (Utils.compareString(out.getOutAddress(), address)) {
                            return true;
                        }

                    }
                }
            }

        }
        return false;
    }
}
