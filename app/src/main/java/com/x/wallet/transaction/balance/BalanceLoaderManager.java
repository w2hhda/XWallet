package com.x.wallet.transaction.balance;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.x.wallet.AppUtils;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.EthUtils;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.BalanceResultBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by wuliang on 18-3-26.
 */

public class BalanceLoaderManager extends BackgroundLoaderManager{
    public static final String ALL_ADDRESS_BALANCE = "all_address_balance";
    private Context mContext;

    public BalanceLoaderManager(Context context) {
        super(context);
        mContext = context;
    }

    public ItemLoadedFuture getBalance(final ItemLoadedCallback<BalanceLoaded> callback) {
        return getBalance(ALL_ADDRESS_BALANCE, callback);
    }

    public ItemLoadedFuture getBalance(String address, final ItemLoadedCallback<BalanceLoaded> callback) {
        Log.i("test3", "BalanceLoaderManager getBalance address = " + address);
        if (TextUtils.isEmpty(address)) {
            return null;
        }

        final boolean taskExists = mPendingTaskUris.contains(address);
        final boolean callbackRequired = (callback != null);

        if (callbackRequired) {
            addCallback(address, callback);
        }

        if (!taskExists) {
            mPendingTaskUris.add(address);
            Log.i("test3", "BalanceLoaderManager getBalance start task.");
            Runnable task = new BalanceTask(address);
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

    public class BalanceTask implements Runnable {
        private final String mAddress;

        public BalanceTask(String address) {
            mAddress = address;
        }

        /** {@inheritDoc} */
        public void run() {
            if(ALL_ADDRESS_BALANCE.equals(mAddress)){
                Cursor cursor = null;
                try{
                    //1.query from db
                    cursor = mContext.getContentResolver().query(
                            XWalletProvider.CONTENT_URI, null, null, null, null);

                    //2.connect address
                    if(cursor != null && cursor.getCount() > 0){
                        StringBuilder builder = new StringBuilder("");
                        while (cursor.moveToNext()){
                            builder.append(EthUtils.addPrefix(cursor.getString(1)));
                            builder.append(",");
                        }
                        String address = builder.toString();
                        address = address.substring(0, address.length() -1);
                        //3.request
                        try{
                            EtherscanAPI.getInstance().getBalances(address, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.e(AppUtils.APP_TAG, "BalanceLoaderManager onFailure IOException", e);
                                    mCallbackHandler.post(new Runnable() {
                                        public void run() {
                                            mCallbacks.remove(mAddress);
                                            mPendingTaskUris.remove(mAddress);
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try{
                                        //1.parse
                                        BalanceResultBean balanceResultBean = new Gson().fromJson(response.body().string(), BalanceResultBean.class);
                                        List<BalanceResultBean.ResultBean> userBeanList = balanceResultBean.getResult();

                                        //2.insert into db
                                        final ArrayList<ContentProviderOperation> rawOperations = new ArrayList<ContentProviderOperation>();
                                        for(BalanceResultBean.ResultBean resultBean : userBeanList){
                                            final ContentProviderOperation.Builder updateBuilder = ContentProviderOperation
                                                    .newUpdate(XWalletProvider.CONTENT_URI);
                                            updateBuilder.withSelection(DbUtils.DbColumns.ADDRESS + " = ?", new String[] {EthUtils.removePrefix(resultBean.getAccount())});
                                            updateBuilder.withValue(DbUtils.DbColumns.BALANCE, resultBean.getBalance());
                                            rawOperations.add(updateBuilder.build());
                                        }
                                        try{
                                            ContentProviderResult[] updateResult = mContext.getContentResolver().applyBatch(XWalletProvider.AUTHORITY, rawOperations);
                                        }catch (Exception e){
                                            Log.e(AppUtils.APP_TAG, "BalanceLoaderManager onResponse exception", e);
                                        }
                                    } finally {
                                        if(response != null){
                                            response.close();
                                        }
                                        mCallbackHandler.post(new Runnable() {
                                            public void run() {
                                                mCallbacks.remove(mAddress);
                                                mPendingTaskUris.remove(mAddress);
                                            }
                                        });
                                    }

                                }
                            });
                        } catch (Exception e){
                            Log.e(AppUtils.APP_TAG, "BalanceLoaderManager BalanceTask exception", e);
                        }
                    } else {
                        mCallbackHandler.post(new Runnable() {
                            public void run() {
                                mCallbacks.remove(mAddress);
                                mPendingTaskUris.remove(mAddress);
                            }
                        });
                    }
                } finally {
                    if(cursor != null){
                        cursor.close();
                    }
                }
            }
        }
    }

    public static class BalanceLoaded {
        public final String mAddress;
        public final String mBalance;

        public BalanceLoaded(String address, String balance) {
            mAddress = address;
            mBalance = balance;
        }
    }

    @Override
    public String getTag() {
        return AppUtils.APP_TAG;
    }
}
