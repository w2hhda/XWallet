package com.x.wallet.lib.btc;

import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.In;
import net.bither.bitherj.core.Out;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.utils.Sha256Hash;
import net.bither.bitherj.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuliang on 18-4-16.
 */

public class CustomeAddressManager {
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
