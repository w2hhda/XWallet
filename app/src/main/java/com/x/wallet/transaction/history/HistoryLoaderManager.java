package com.x.wallet.transaction.history;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.TokenResultBean;
import com.x.wallet.lib.eth.data.TransactionsResultBean;
import com.x.wallet.transaction.balance.BalanceLoaderManager;
import com.x.wallet.transaction.balance.ItemLoadedCallback;
import com.x.wallet.transaction.balance.ItemLoadedFuture;
import com.x.wallet.transaction.token.BackgroundLoaderManager;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HistoryLoaderManager extends BackgroundLoaderManager {
    public static final String NORMAL_HISTORY = "normal_history";
    public static final String TOKEN_HISTORY = "token_history";
    private Context mContext;

    public HistoryLoaderManager(Context context) {
        super(context);
        mContext = context;
    }
    public ItemLoadedFuture getNormalHistory(final String address, final ItemLoadedCallback<HistoryLoaded> callback) {
        return getHistory(Uri.parse(NORMAL_HISTORY),address,null,  callback);
    }

    public ItemLoadedFuture getTokenHistory(final String address,final String contractAddress, final ItemLoadedCallback<HistoryLoaded> callback) {
        return getHistory(Uri.parse(TOKEN_HISTORY), address,contractAddress,  callback);
    }

    public ItemLoadedFuture getHistory(Uri uri, String address,final String contractAddress, final ItemLoadedCallback<HistoryLoaderManager.HistoryLoaded> callback) {
        Log.i(AppUtils.APP_TAG, "HistoryLoaderManager getHistory uri = " + uri);
        if (uri == null) {
            return null;
        }

        final boolean taskExists = mPendingTaskUris.contains(uri);
        final boolean callbackRequired = (callback != null);

        if (callbackRequired) {
            addCallback(uri, callback);
        }
        Log.i(AppUtils.APP_TAG, "HistoryLoaderManager taskExists = " + taskExists);
        if (!taskExists) {
            mPendingTaskUris.add(uri);
            Log.i(AppUtils.APP_TAG, "HistoryLoaderManager getHistory start task.");
            Runnable task = new HistoryTask(uri, address, contractAddress);
            mExecutor.execute(task);
        }
        return new ItemLoadedFuture() {
            private boolean mIsDone;

            public void cancel(Uri uri) {
                cancelCallback(callback);
            }

            public void setIsDone(boolean done) {
                mIsDone = done;
            }

            public boolean isDone() {
                return mIsDone;
            }
        };
    }


    public class HistoryTask implements Runnable{
        private final Uri mUri;
        private final String address;
        private final String contractAddress;

        public HistoryTask(Uri mUri, String address, String contractAddress) {
            this.mUri = mUri;
            this.address = address;
            this.contractAddress = contractAddress;
        }

        @Override
        public void run() {
            switch (mUri.toString()){
                case NORMAL_HISTORY:
                    //getNormalHistory(address);
                    requestTxList(address);
                    break;
                case TOKEN_HISTORY:
                    requestTokenTxList(address, contractAddress);
                    //getTokenHistory(address, contractAddress);
                    break;
                    default:
                        break;
            }
        }

        private void getNormalHistory(String address){
            if (AppUtils.isFirstTimeToSyncTxList(address, address)){ //get all normal tx list
                try {
                    AppUtils.log("get all normal tx list");
                    EtherscanAPI.getInstance().getNormalTransactions(address, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            AppUtils.log("get normal history fail exception" + e);
                            removeCallback();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                ResponseBody body = response.body();
                                if(body != null){
                                    handleNormalHistory(body.string());
                                }
                            } finally {
                                handleCallback();
                            }
                        }
                    },true);
                }catch (IOException e){
                    handleCallback();
                }finally {
                    AppUtils.updateSyncAddress(address, address);
                }
            }else {                 //get last 50 normal tx list
                try {
                    AppUtils.log("get last 50 normal tx list");
                    EtherscanAPI.getInstance().getLastTxHistory(address, 0, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            AppUtils.log("get normal history fail exception" + e);
                            removeCallback();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                ResponseBody body = response.body();
                                if(body != null){
                                    handleNormalHistory(body.string());
                                }
                            } finally {
                                handleCallback();
                            }
                        }
                    });
                }catch (IOException e){
                    handleCallback();
                }
            }
        }

        private void getTokenHistory(String address, String contractAddress){
            if (AppUtils.isFirstTimeToSyncTxList(address, contractAddress)){  //get all token tx list
                AppUtils.log("get all token tx list");

                try {
                    EtherscanAPI.getInstance().getTokenTxHistory(address, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            AppUtils.log("get all token history fail exception" + e);
                            removeCallback();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                ResponseBody body = response.body();
                                if(body != null){
                                    handleTokenHistory(body.string());
                                }
                            } finally {
                                handleCallback();
                            }
                        }
                    });
                }catch (IOException e){
                    removeCallback();
                } finally {
                    AppUtils.updateSyncAddress(address, contractAddress);
                }
            }else {  //get last 50 token tx list
                try {
                    AppUtils.log("get last 50 token tx list");
                    EtherscanAPI.getInstance().getLastTokenTxHistory(address, contractAddress, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            AppUtils.log("get last token history fail exception" + e);
                            removeCallback();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                ResponseBody body = response.body();
                                if(body != null){
                                    handleTokenHistory(body.string());
                                }
                            } finally {
                                handleCallback();
                            }
                        }
                    });
                }catch (IOException e){
                    removeCallback();
                }
            }

        }

        private void handleNormalHistory(String result){
            if (result == null){
                handleCallback();
                return;
            }

            TransactionsResultBean bean = new Gson().fromJson(result, TransactionsResultBean.class);
            if (!bean.getMessage().equalsIgnoreCase("OK")){
                AppUtils.log("get normal history fail for wrong response" + result);
                handleCallback();
                return;
            }
            //1.phrase
            List<TransactionsResultBean.ReceiptBean> receiptBeans = bean.getResult();

            //insert db
            insertNormalTx(receiptBeans);
        }

        private void handleTokenHistory(String result){
            if (result == null){
                handleCallback();
                return;
            }

            TokenResultBean bean = new Gson().fromJson(result, TokenResultBean.class);
            if (!bean.getMessage().equalsIgnoreCase("OK")){
                AppUtils.log("get normal history fail for wrong response" + result);
                handleCallback();
                return;
            }
            //1.phrase
            List<TokenResultBean.ResultBean> receiptBeans = bean.getResult();

            //insert db
            insertTokenTx(receiptBeans);
        }

        private void insertNormalTx(List<TransactionsResultBean.ReceiptBean> beans){
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

        private void insertTokenTx(List<TokenResultBean.ResultBean> beans){
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

        private ContentValues getNormalContentValues(TransactionsResultBean.ReceiptBean bean){
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

        private ContentValues getTokenContentValues(TokenResultBean.ResultBean bean){
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

        private ContentValues getTokenUpdateValues(TokenResultBean.ResultBean bean){
            ContentValues values = new ContentValues();
            values.put(DbUtils.TxTableColumns.CONTRACT_ADDRESS, bean.getContractAddress());
            values.put(DbUtils.TxTableColumns.VALUE, bean.getValue());
            values.put(DbUtils.TxTableColumns.TO_ADDRESS, bean.getTo());
            values.put(DbUtils.TxTableColumns.TOKEN_SYMBOL, bean.getTokenSymbol());
            values.put(DbUtils.TxTableColumns.TOKEN_NAME, bean.getTokenName());
            values.put(DbUtils.TxTableColumns.TOKEN_DECIMALS, bean.getTokenDecimal());

            return values;
        }

        private void removeCallback(){
            mCallbackHandler.post(new Runnable() {
                public void run() {
                    mCallbacks.remove(mUri);
                    mPendingTaskUris.remove(mUri);
                }
            });
        }

        private void handleCallback(){
            mCallbackHandler.post(new Runnable() {
                public void run() {
                    final Set<ItemLoadedCallback> callbacks = mCallbacks.get(mUri);
                    if (callbacks != null) {
                        // Make a copy so that the callback can unregister itself
                        for (final ItemLoadedCallback<HistoryLoaderManager.HistoryLoaded> callback : asList(callbacks)) {
                            callback.onItemLoaded(null, null);
                        }
                    }

                    mCallbacks.remove(mUri);
                    mPendingTaskUris.remove(mUri);
                }
            });
        }

        private void requestTxList(String address){
            TxRetrofitClient.requestTxList(address, new TxRetrofitClient.OnRequestFinishedListener() {
                @Override
                public void onRequestFinished() {
                    handleCallback();
                }
            });
        }

        private void requestTokenTxList(String address, String contractAddress){
            TxRetrofitClient.requestTokenTxList(address, contractAddress, new TxRetrofitClient.OnRequestFinishedListener() {
                @Override
                public void onRequestFinished() {
                    handleCallback();
                }
            });
        }
    }

    public static class HistoryLoaded {
        public HistoryLoaded() {
        }
    }

    @Override
    public String getTag() {
        return AppUtils.APP_TAG;
    }
}
