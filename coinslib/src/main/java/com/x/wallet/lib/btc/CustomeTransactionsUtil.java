package com.x.wallet.lib.btc;

import android.util.Log;

import com.google.gson.Gson;

import net.bither.bitherj.api.BlockChainMytransactionsApi;
import net.bither.bitherj.core.Block;
import net.bither.bitherj.core.BlockChain;
import net.bither.bitherj.core.In;
import net.bither.bitherj.core.Out;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.utils.TransactionsUtil;
import net.bither.bitherj.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuliang on 18-4-16.
 */

public class CustomeTransactionsUtil {
    private static final String BLOCK_CHAIN_HEIGHT = "height";
    private static final String BLOCK_CHAIN_CNT = "n_tx";

    public static void getMyTxFromBither(List<String> addressList) throws Exception {
        Log.i("testGetTx", "TransactionsUtil getMyTxFromBither");
        getTxForAddress(1, addressList);
    }

    private static void getTxForAddress(final int webType, List<String> addressList) throws Exception {
        for (String address : addressList) {
            Block storedBlock = BlockChain.getInstance().getLastBlock();
            int storeBlockHeight = storedBlock.getBlockNo();
            int apiBlockCount = 0;
            int txSum = 0;
            boolean needGetTxs = true;
            int page = 1;
            // TODO
            List<Tx> transactions = new ArrayList<Tx>();

            while (needGetTxs) {
                BlockChainMytransactionsApi blockChainMytransactionsApi = new BlockChainMytransactionsApi(address);
                blockChainMytransactionsApi.handleHttpGet();
                String txResult = blockChainMytransactionsApi.getResult();
                JSONObject jsonObject = new JSONObject(txResult);
                // TODO: get the latest block number from blockChain.info
                JSONObject jsonObjectBlockChain = getLatestBlockNumberFromBlockchain();
                if (!jsonObjectBlockChain.isNull(BLOCK_CHAIN_HEIGHT)) {
                    apiBlockCount = jsonObjectBlockChain.getInt(BLOCK_CHAIN_HEIGHT);
                }
                int txCnt = jsonObject.getInt(BLOCK_CHAIN_CNT);
                // TODO: get transactions from blockChain.info
                transactions = CustomeTransactionsUtil.getTransactionsFromBlockChain(txResult, storeBlockHeight);
                transactions = CustomeAddressManager.compressTxsForApi(transactions, address);

                Collections.sort(transactions, new TransactionsUtil.ComparatorTx());
                if(transactions != null){
                    Log.i("testGetTx", " transactions.size = " + transactions);
                }
                CustomeAddress.initTxs2(transactions);
                //address.initTxs(transactions);
                txSum = txSum + transactions.size();
                needGetTxs = false;
            }

            if (apiBlockCount < storeBlockHeight && storeBlockHeight - apiBlockCount < 100) {
                BlockChain.getInstance().rollbackBlock(apiBlockCount);
            }
            AbstractDb.txProvider.updateSyncComplete(address, true);
        }
    }

    private static JSONObject getLatestBlockNumberFromBlockchain() throws Exception {
        BlockChainMytransactionsApi blockChainMytransactionsApi = new BlockChainMytransactionsApi();
        blockChainMytransactionsApi.handleHttpGet();
        String txResultBlockChain = blockChainMytransactionsApi.getResult();
        return new JSONObject(txResultBlockChain);
    }

    private  static List<Tx> getTransactionsFromBlockChain(
            String result, int storeBlockHeight) throws Exception {
        System.out.println("testGetTx TransactionsUtil getTransactionsFromBlockChain 0");
        List<Tx> transactions = new ArrayList<Tx>();
        List<Block> blocks = AbstractDb.blockProvider.getAllBlocks();
        Map<Integer, Integer> blockMapList = new HashMap<Integer, Integer>();
        int minBlockNo = blocks.get(blocks.size() - 1).getBlockNo();
        for (Block block : blocks) {
            blockMapList.put(block.getBlockNo(), block.getBlockTime());
            if (minBlockNo > block.getBlockNo()) {
                minBlockNo = block.getBlockNo();
            }
        }

        RawaddrResultBean rawaddrResultBean = new Gson().fromJson(result, RawaddrResultBean.class);
        List<RawaddrResultBean.Tx> txBeanList = rawaddrResultBean.getTxs();
        if(txBeanList != null){
            System.out.println("testGetTx TransactionsUtil getTransactionsFromBlockChain 1");
            HashMap<String, String> txIdMap = new HashMap<String, String>();
            for(RawaddrResultBean.Tx txBean : txBeanList){
                String hash = txBean.getHash();
                RawtxResultBean rawtxResultBean = queryTxByHash(hash);
                RawtxResultBean.Data data = rawtxResultBean.getData();
                if(data != null){
                    List<RawtxResultBean.InputBean> inputBeanList = data.getInputs();
                    for(RawtxResultBean.InputBean inputBean : inputBeanList){
                        txIdMap.put(inputBean.getScript_hex(), inputBean.getReceived_from().getTxid());
                        System.out.println("testGetTx 2 script = " + inputBean.getScript_hex() + ", txId = " + inputBean.getReceived_from().getTxid());
                    }
                }
                Tx tx = new Tx();
                tx.setTxHash(Utils.reverseBytes(Utils.hexStringToByteArray(hash)));
                tx.setTxVer(txBean.getVer());
                tx.setTxLockTime(txBean.getLock_time());
                tx.setTxTime(txBean.getTime());
                tx.setBlockNo(txBean.getBlock_height());

                List<RawaddrResultBean.Input> inBeanList = txBean.getInputs();
                if(inBeanList != null){
                    System.out.println("testGetTx TransactionsUtil getTransactionsFromBlockChain 2");
                    for(RawaddrResultBean.Input inputBean : inBeanList){
                        In in = new In();
                        in.setTxHash(tx.getTxHash());
                        in.setInSignature(Utils.hexStringToByteArray(inputBean.getScript()));
                        in.setInSequence(inputBean.getSequence());
                        RawaddrResultBean.PrivOut privOut = inputBean.getPrev_out();
                        if(privOut != null){
                            in.setPrevOutSn(privOut.getN());
                            in.setPrevOutScript(Utils.hexStringToByteArray(privOut.getScript()));
                        }
                        String txId = txIdMap.get(inputBean.getScript());
                        System.out.println("testGetTx script = " + privOut.getScript() + ", txId = " + txId);
                        if(txId != null){
                            in.setPrevTxHash(Utils.reverseBytes(Utils.hexStringToByteArray(txId)));
                        }
                        tx.addInput(in);
                    }
                }
                List<RawaddrResultBean.Out> outBeanList = txBean.getOut();
                if(outBeanList != null){
                    System.out.println("testGetTx TransactionsUtil getTransactionsFromBlockChain 3");
                    for(RawaddrResultBean.Out outBean : outBeanList){
                        Out out = new Out();
                        out.setTxHash(tx.getTxHash());
                        out.setOutSn(outBean.getN());
                        out.setOutScript(Utils.hexStringToByteArray(outBean.getScript()));
                        out.setOutValue(outBean.getValue());
                        out.setOutAddress(outBean.getAddr());
                        tx.addOutput(out);
                    }
                }
                transactions.add(tx);
            }
        }
        System.out.println("testGetTx TransactionsUtil getTransactionsFromBlockChain size = " + transactions.size());
        return transactions;
    }

    public static RawtxResultBean queryTxByHash(String hash){
        try{
            SoChainMytransactionsApi soChainMytransactionsApi = new SoChainMytransactionsApi("https://chain.so/api/v2/tx/BTC/" + hash);
            soChainMytransactionsApi.handleHttpGet();
            String txResult = soChainMytransactionsApi.getResult();
            return txResult != null ? new Gson().fromJson(txResult, RawtxResultBean.class) : null;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}