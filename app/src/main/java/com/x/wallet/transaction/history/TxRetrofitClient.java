package com.x.wallet.transaction.history;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.TokenResultBean;
import com.x.wallet.lib.eth.data.TransactionsResultBean;
import com.x.wallet.transaction.balance.RetrofitClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by wuliang on 18-4-4.
 */

public class TxRetrofitClient {
    private static final String TAG = "TxRetrofitClient";
    private static Retrofit retrofitEth = null;
    private static final int MAX_TX_IN_PAGE = 100;

    private static Retrofit getEthClient(String baseUrl) {
        if (retrofitEth == null) {
            OkHttpClient client = new OkHttpClient.Builder().
                    connectTimeout(20, TimeUnit.SECONDS).
                    readTimeout(20, TimeUnit.SECONDS).
                    writeTimeout(20, TimeUnit.SECONDS).build();
            retrofitEth = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofitEth;
    }

    private static RetrofitTxService getEthService() {
        return getEthClient(RetrofitClient.BALANCE_BASE_URL).create(RetrofitTxService.class);
    }

    public static void requestTxList(String address, final OnRequestFinishedListener listener) {
        boolean needGetTx = true;
        int page = 1;
        while (needGetTx){
            String lastBlockNo = TxHelper.getLastTxBlockNo(address);
            Call<TransactionsResultBean>  call = createTxResultBeanObservable(address, lastBlockNo, page);
            try {
                Response<TransactionsResultBean> transactionsResultBean = call.execute();
                TransactionsResultBean resultBean = transactionsResultBean.body();
                if(resultBean != null && resultBean.getMessage().equalsIgnoreCase("OK")){
                    //1.phrase
                    List<TransactionsResultBean.ReceiptBean> receiptBeans = resultBean.getResult();
                    if(receiptBeans != null){
                        int txSize = receiptBeans.size();
                        needGetTx = txSize >= MAX_TX_IN_PAGE;
                        if(txSize > 0){
                            //insert db
                            insertNormalTx(receiptBeans);
                            String newLastBlockNo = receiptBeans.get(txSize - 1).getBlockNumber();
                            if(newLastBlockNo.equals(lastBlockNo)){
                                page ++;
                            } else {
                                page = 1;
                                TxHelper.updateLastTxBlockNo(address, newLastBlockNo);
                            }
                        }
                    } else {
                        needGetTx = false;
                    }
                } else {
                    needGetTx = false;
                }
            } catch (Exception e){
                Log.i(AppUtils.APP_TAG, "TxRetrofitClient requestTxList Exception", e);
                needGetTx = false;
            }
        }
        if(listener != null){
            listener.onRequestFinished();
        }
    }

    public static void requestTokenTxList(String address, String contractAddress, final OnRequestFinishedListener listener) {
        boolean needGetTx = true;
        while (needGetTx){
            long page = TxHelper.getLastTokenTxPage(address, contractAddress);
            Call<TokenResultBean>  call = createTokenTxResultBeanObservable(address, contractAddress, page);
            try {
                Response<TokenResultBean> transactionsResultBean = call.execute();
                TokenResultBean resultBean = transactionsResultBean.body();
                if(resultBean != null && resultBean.getMessage().equalsIgnoreCase("OK")){
                    List<TokenResultBean.ResultBean> receiptBeans = resultBean.getResult();
                    if(receiptBeans != null){
                        int txSize = receiptBeans.size();
                        needGetTx = txSize >= MAX_TX_IN_PAGE;
                        if(txSize > 0){
                            insertTokenTx(receiptBeans);
                            page ++;
                            TxHelper.updateLastTokenTxPage(address, contractAddress, page);
                        }
                    } else {
                        needGetTx = false;
                    }
                } else {
                    needGetTx = false;
                }
            } catch (Exception e){
                Log.i(AppUtils.APP_TAG, "TxRetrofitClient requestTokenTxList Exception", e);
                needGetTx = false;
            }
        }
        if(listener != null){
            listener.onRequestFinished();
        }
    }

    private static void insertNormalTx(List<TransactionsResultBean.ReceiptBean> beans){
        for(TransactionsResultBean.ReceiptBean bean: beans){
            ContentValues values = getNormalContentValues(bean);
            if (DbUtils.isTxNeedUpdate(bean.getHash(), bean.getBlockNumber())){
                AppUtils.log("tx need update:" + bean.getHash() );
                int count = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                        .update(XWalletProvider.CONTENT_URI_TRANSACTION, values,
                                DbUtils.TxTableColumns.TX_HASH + " = ?",
                                new String[]{bean.getHash()});
                AppUtils.log("insertNormal update tx hash = " + bean.getHash());
                continue;
            }

            if (DbUtils.isTxExist(bean.getHash())){
                continue;
            }
            //insert into db
            Uri uri = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                    .insert(XWalletProvider.CONTENT_URI_TRANSACTION, values);
            AppUtils.log("insertNormal add tx uri = " + uri);
        }
    }

    private static ContentValues getNormalContentValues(TransactionsResultBean.ReceiptBean bean){
        ContentValues values = new ContentValues();
        values.put(DbUtils.TxTableColumns.TX_HASH, bean.getHash());                 //1
        values.put(DbUtils.TxTableColumns.TIME_STAMP, bean.getTimeStamp());         //2
        values.put(DbUtils.TxTableColumns.NONCE, bean.getNonce());                  //3
        values.put(DbUtils.TxTableColumns.FROM_ADDRESS, bean.getFrom());            //4
        values.put(DbUtils.TxTableColumns.TO_ADDRESS, bean.getTo());                //5
        values.put(DbUtils.TxTableColumns.VALUE, bean.getValue());                  //6
        values.put(DbUtils.TxTableColumns.GAS_LIMIT, bean.getGas());                //7
        values.put(DbUtils.TxTableColumns.GAS_PRICE, bean.getGasPrice());           //8
        values.put(DbUtils.TxTableColumns.IS_ERROR, bean.getIsError());             //9
        values.put(DbUtils.TxTableColumns.TX_RECEIPT_STATUS, bean.getTxreceipt_status());     //10
        values.put(DbUtils.TxTableColumns.INPUT_DATA, bean.getInput());             //11
        values.put(DbUtils.TxTableColumns.GAS_USED, bean.getGasUsed());             //12
        values.put(DbUtils.TxTableColumns.CONTRACT_ADDRESS, bean.getContractAddress());     //13
        values.put(DbUtils.TxTableColumns.BLOCK_NUMBER, bean.getBlockNumber());     //14
        return values;
    }

    private static Call<TransactionsResultBean> createTxResultBeanObservable(String address, String blockNo, int page) {
        return getEthService().getTxList("account", "txlist", address,
                blockNo, "99999999", page,
                MAX_TX_IN_PAGE, "asc", EtherscanAPI.API_KEY);
    }

    private static Call<TokenResultBean> createTokenTxResultBeanObservable(String address, String contractAddress, long page) {
        return getEthService().getTokenTxList("account", "tokentx", contractAddress,
                address,  page, MAX_TX_IN_PAGE, "asc", EtherscanAPI.API_KEY);
    }

    private static void insertTokenTx(List<TokenResultBean.ResultBean> beans){
        for(TokenResultBean.ResultBean bean: beans){
            ContentValues values = getTokenContentValues(bean);
            if (DbUtils.isTxNeedUpdate(bean.getHash(), bean.getBlockNumber())){
                AppUtils.log("tx need update:" + bean.getHash() );
                int count = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                        .update(XWalletProvider.CONTENT_URI_TRANSACTION, values,
                                DbUtils.TxTableColumns.TX_HASH + " = ?",
                                new String[]{bean.getHash()});
                AppUtils.log("insertToken update tx count = " + count);
                continue;
            } //new token tx

            if (DbUtils.isTokenNeedUpdate(bean.getHash(), bean.getBlockNumber())){
                AppUtils.log("tx need update:" + bean.getHash() );
                int count = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                        .update(XWalletProvider.CONTENT_URI_TRANSACTION, getTokenUpdateValues(bean),
                                DbUtils.TxTableColumns.TX_HASH + " = ?",
                                new String[]{bean.getHash()});
                AppUtils.log("insertToken update tx count = " + count);
                continue;
            } //have added by normal tx, need to update some columns.

            if (DbUtils.isTxExist(bean.getHash())){
                continue;
            }

            //insert into db
            Uri uri = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                    .insert(XWalletProvider.CONTENT_URI_TRANSACTION, values);
            AppUtils.log("insertToken add tx uri = " + uri);

        }

    }

    private static ContentValues getTokenContentValues(TokenResultBean.ResultBean bean){
        ContentValues values = new ContentValues();
        values.put(DbUtils.TxTableColumns.TX_HASH, bean.getHash());
        values.put(DbUtils.TxTableColumns.TIME_STAMP, bean.getTimeStamp());
        values.put(DbUtils.TxTableColumns.NONCE, bean.getNonce());
        values.put(DbUtils.TxTableColumns.FROM_ADDRESS, bean.getFrom());
        values.put(DbUtils.TxTableColumns.TO_ADDRESS, bean.getTo());
        values.put(DbUtils.TxTableColumns.VALUE, bean.getValue());
        values.put(DbUtils.TxTableColumns.GAS_LIMIT, bean.getGas());
        values.put(DbUtils.TxTableColumns.GAS_PRICE, bean.getGasPrice());
        values.put(DbUtils.TxTableColumns.TOKEN_NAME, bean.getTokenName());
        values.put(DbUtils.TxTableColumns.TOKEN_DECIMALS, bean.getTokenDecimal());
        values.put(DbUtils.TxTableColumns.INPUT_DATA, bean.getInput());
        values.put(DbUtils.TxTableColumns.GAS_USED, bean.getGasUsed());
        values.put(DbUtils.TxTableColumns.CONTRACT_ADDRESS, bean.getContractAddress());
        values.put(DbUtils.TxTableColumns.BLOCK_NUMBER, bean.getBlockNumber());
        values.put(DbUtils.TxTableColumns.TOKEN_SYMBOL, bean.getTokenSymbol());

        return values;
    }

    private static ContentValues getTokenUpdateValues(TokenResultBean.ResultBean bean){
        ContentValues values = new ContentValues();
        values.put(DbUtils.TxTableColumns.CONTRACT_ADDRESS, bean.getContractAddress());
        values.put(DbUtils.TxTableColumns.VALUE, bean.getValue());
        values.put(DbUtils.TxTableColumns.TO_ADDRESS, bean.getTo());
        values.put(DbUtils.TxTableColumns.TOKEN_SYMBOL, bean.getTokenSymbol());
        values.put(DbUtils.TxTableColumns.TOKEN_NAME, bean.getTokenName());
        values.put(DbUtils.TxTableColumns.TOKEN_DECIMALS, bean.getTokenDecimal());

        return values;
    }

    public interface OnRequestFinishedListener {
        void onRequestFinished();
    }
}
