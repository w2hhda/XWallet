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

package com.x.wallet.lib.btc;

import android.util.Log;

import net.bither.bitherj.core.BlockChain;
import net.bither.bitherj.core.Out;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.utils.Utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CustomeTxBuilder {
    public static TxBuildResult buildTx(String fromAddress, String changeAddress,
                                 List<Long> amounts, List<String> toAddresses,
                                 int feeBase){
        if (Utils.isEmpty(changeAddress)) {
            changeAddress = fromAddress;
        }
        long value = 0;
        for (long amount : amounts) {
            value += amount;
        }

        List<Tx> unspendTxs;
        List<Out> unspendOuts;
        unspendTxs = AbstractDb.txProvider.getUnspendTxWithAddress(fromAddress);
        unspendOuts = getUnspendOuts(unspendTxs);

        List<Out> canSpendOuts = getCanSpendOuts(unspendTxs);
        List<Out> canNotSpendOuts = getCanNotSpendOuts(unspendTxs);
        if (value > getAmount(unspendOuts)) {
            //throw new TxBuilderException.TxBuilderNotEnoughMoneyException(value - getAmount(unspendOuts));
            return new TxBuildResult(null, TxBuildResult.ResultCode.ERROR_NOT_ENOUGH_MONEY);
        } else if (value > getAmount(canSpendOuts)) {
            //throw new TxBuilderException.TxBuilderWaitConfirmException(getAmount(canNotSpendOuts));
            return new TxBuildResult(null, TxBuildResult.ResultCode.ERROR_WAIT_CONFIRM);
        } else if (value == getAmount(unspendOuts) && getAmount(canNotSpendOuts) != 0) {
            // there is some unconfirm tx, it will not empty wallet
            //throw new TxBuilderException.TxBuilderWaitConfirmException(TxBuilder.getAmount(canNotSpendOuts));
            return new TxBuildResult(null, TxBuildResult.ResultCode.ERROR_WAIT_CONFIRM);
        }

        Tx tx = buildTx(changeAddress, unspendTxs, prepareTx(amounts, toAddresses), feeBase);
        if(tx == null){
            return new TxBuildResult(null, TxBuildResult.ResultCode.ERROR_UNKNOWN);
        }
        return new TxBuildResult(tx, TxBuildResult.ResultCode.RESULT_OK);
    }

    private static Tx buildTx(String changeAddress, List<Tx> unspendTxs, Tx tx, int feeBase) {
        List<Out> outs = getUnspendOuts(unspendTxs);
        Collections.sort(outs, new Comparator<Out>() {
            public int compare(Out out1, Out out2) {
                int depth1 = 0;
                int depth2 = 0;
                long coinDepth1 = BlockChain.getInstance().lastBlock.getBlockNo() * out1.getOutValue() - out1.getCoinDepth() + out1.getOutValue();
                long coinDepth2 = BlockChain.getInstance().lastBlock.getBlockNo() * out2.getOutValue() - out2.getCoinDepth() + out2.getOutValue();
                if (coinDepth1 != coinDepth2) {
                    if (coinDepth2 > coinDepth1)
                        return 1;
                    else
                        return -1;
                } else if (out1.getOutValue() != out2.getOutValue()) {
                    if (out2.getOutValue() > out1.getOutValue())
                        return 1;
                    else
                        return -1;
                } else {
                    BigInteger hash1 = new BigInteger(1, out1.getTxHash());
                    BigInteger hash2 = new BigInteger(1, out2.getTxHash());
                    int result = hash1.compareTo(hash2);
                    if (result != 0) {
                        return result;
                    } else {
                        return out1.getOutSn() - out2.getOutSn();
                    }
                }
            }
        });

        long spendOutValue = 0;
        for (Out out : tx.getOuts()) {
            spendOutValue += out.getOutValue();
        }
        int outCount = tx.getOuts().size();
        //Log.i("test", "CustomeTxBuilder buildTx spendOutValue = " + spendOutValue);
        //Log.i("test", "CustomeTxBuilder buildTx allout = " + getAmount(outs));
        return buildTx(outs, spendOutValue, outCount, tx, changeAddress, feeBase);
    }

    private static Tx buildTx(List<Out> outs, long spendOutValue, int outCount, Tx tx, String changeAddress, int feeBase) {
        int inSize = outs.size();
        long fee = 0;
        List<Out> result = new ArrayList<Out>();
        long sum = 0;
        for (Out out : outs) {
            sum += out.getOutValue();
            result.add(out);
            if (sum > spendOutValue) {
                fee = calculateTxSize(result.size(), outCount) * feeBase;
                long change = sum - spendOutValue - fee;
                if(change == 0) {
                    addInput(tx, result);
                    tx.setSource(Tx.SourceType.self.getValue());
                    //Log.i("test", "CustomeAddress buildTx 1 result.size = " + result.size());
                    return tx;
                } else if(change < 0) {
                    if(result.size() == inSize) {
                        return null;
                    } else {
                        continue;
                    }
                }
                fee = calculateTxSize(result.size(), outCount + 1) * feeBase;
                change = sum - spendOutValue - fee;
                if(change > 0) {
                    addInput(tx, result);
                    tx.setSource(Tx.SourceType.self.getValue());

                    Out changeOutput = new Out();
                    changeOutput.setOutValue(change);
                    changeOutput.setOutAddress(changeAddress);
                    tx.addOutput(changeOutput.getOutValue(), changeOutput.getOutAddress());
                    //Log.i("test", "CustomeAddress buildTx 2 result.size = " + result.size());
                    return tx;
                }
                if(result.size() == inSize) {
                    return null;
                }
            }
        }
        return null;
    }

    private static void addInput(Tx tx, List<Out> outs){
        for (Out out : outs) {
            //Log.i("test", "CustomeAddress addInput out.getOutValue = " + out.getOutValue());
            tx.addInput(out);
        }
    }

    private static List<Out> getUnspendOuts(List<Tx> txs) {
        List<Out> result = new ArrayList<Out>();
        for (Tx tx : txs) {
            result.add(tx.getOuts().get(0));
        }
        return result;
    }

    private static List<Out> getCanSpendOuts(List<Tx> txs) {
        List<Out> result = new ArrayList<Out>();
        for (Tx tx : txs) {
//            if (tx.getBlockNo() != Tx.TX_UNCONFIRMED || tx.getSource() == Tx.SourceType.self.getValue()) {
            result.add(tx.getOuts().get(0));
//            }
        }
        return result;
    }

    private static List<Out> getCanNotSpendOuts(List<Tx> txs) {
        List<Out> result = new ArrayList<Out>();
//        for (Tx tx : txs) {
//            if (tx.getBlockNo() == Tx.TX_UNCONFIRMED && tx.getSource() == Tx.SourceType.network.getValue()) {
//                result.add(tx.getOuts().get(0));
//            }
//        }
        return result;
    }

    private static long getAmount(List<Out> outs) {
        long amount = 0;
        for (Out out : outs) {
            amount += out.getOutValue();
        }
        return amount;
    }

    private static Tx prepareTx(List<Long> amounts, List<String> addresses) {
        Tx tx = new Tx();
        for (int i = 0; i < amounts.size(); i++) {
            tx.addOutput(amounts.get(i), addresses.get(i));
        }
        return tx;
    }

    private static long calculateTxSize(int inCount, int outCount){
        return 181 * inCount + 34 * outCount + 10;
    }
}