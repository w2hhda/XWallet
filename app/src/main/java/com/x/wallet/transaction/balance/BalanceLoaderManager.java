package com.x.wallet.transaction.balance;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.BalanceResultBean;
import com.x.wallet.lib.eth.data.PriceResultBean;
import com.x.wallet.transaction.token.BackgroundLoaderManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by wuliang on 18-3-26.
 */

public class BalanceLoaderManager extends BackgroundLoaderManager {
    public static final String ALL_BALANCE = "all_balance";
    public static final String ALL_ADDRESS_BALANCE = "all_address_balance";
    private Context mContext;

    public BalanceLoaderManager(Context context) {
        super(context);
        mContext = context;
    }

    public ItemLoadedFuture getOneAccountBalance(Uri uri) {
        return getBalance(uri, null, false);
    }

    public ItemLoadedFuture getAllBalance(final ItemLoadedCallback<BalanceLoaded> callback, boolean isNeedAutoAddToken) {
        return getBalance(Uri.parse(ALL_BALANCE), callback, isNeedAutoAddToken);
    }

    public ItemLoadedFuture getBalance(final ItemLoadedCallback<BalanceLoaded> callback) {
        return getBalance(Uri.parse(ALL_ADDRESS_BALANCE), callback, false);
    }

    public ItemLoadedFuture getBalance(Uri uri, final ItemLoadedCallback<BalanceLoaded> callback, boolean isNeedAutoAddToken) {
        Log.i(AppUtils.APP_TAG, "BalanceLoaderManager getBalance uri = " + uri);
        if (uri == null) {
            return null;
        }

        final boolean taskExists = mPendingTaskUris.contains(uri);
        final boolean callbackRequired = (callback != null);

        if (callbackRequired) {
            addCallback(uri, callback);
        }
        Log.i(AppUtils.APP_TAG, "BalanceLoaderManager taskExists = " + taskExists);
        if (!taskExists) {
            mPendingTaskUris.add(uri);
            Log.i(AppUtils.APP_TAG, "BalanceLoaderManager getBalance start task.");
            Runnable task = new BalanceTask(uri, isNeedAutoAddToken);
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
        private final Uri mUri;
        private final boolean mIsNeedAutoAddToken;

        public BalanceTask(Uri address, boolean isNeedAutoAddToken) {
            mUri = address;
            mIsNeedAutoAddToken = isNeedAutoAddToken;
        }

        /** {@inheritDoc} */
        public void run() {
            Log.i(AppUtils.APP_TAG, "BalanceLoaderManager run mUri = " + mUri);
            if(ALL_BALANCE.equals(mUri.toString())){
                RetrofitClient.requestBalance(new RetrofitClient.OnRequestFinishedListener() {
                    @Override
                    public void onRequestFinished() {
                        handleCallback();
                    }
                }, mIsNeedAutoAddToken);
            }else if(ALL_ADDRESS_BALANCE.equals(mUri.toString())){
                String allEthAddress = DbUtils.queryAllEthAddress();
                if(TextUtils.isEmpty(allEthAddress)){
                    handleCallback();
                    return;
                }
                getEtherPrice(allEthAddress);
            } else if(mUri.toString().startsWith(XWalletProvider.CONTENT_URI_TOKEN.toString())){
                String address = mUri.getLastPathSegment();
                requestBalanceForToken(address);
            } else if(mUri.toString().startsWith(XWalletProvider.CONTENT_URI.toString())){
                String address = mUri.getLastPathSegment();
                SingleEthAccountBalanceRetrofitClient.requestBalance(null, address);
            }
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
                        for (final ItemLoadedCallback<BalanceLoaded> callback : asList(callbacks)) {
                            callback.onItemLoaded(null, null);
                        }
                    }

                    mCallbacks.remove(mUri);
                    mPendingTaskUris.remove(mUri);
                }
            });
        }

        private void getEtherPrice(final String address){
            try{
                EtherscanAPI.getInstance().getEtherPrice(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(AppUtils.APP_TAG, "BalanceLoaderManager getEtherPrice onFailure exception", e);
                        handleCallback();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try{
                            ResponseBody body = response.body();
                            if(body != null){
                                //1.parse
                                PriceResultBean priceResultBean = new Gson().fromJson(body.string(), PriceResultBean.class);
                                BalanceConversionUtils.write(priceResultBean.getResult().getEthusd());
                                Log.i(AppUtils.APP_TAG, "BalanceLoaderManager getEtherPrice onResponse EthToUsd = " + priceResultBean.getResult().getEthusd());
                                requestBalance(address);
                            } else {
                                handleCallback();
                            }
                        } catch (Exception e){
                            Log.i(AppUtils.APP_TAG, "BalanceLoaderManager getEtherPrice onResponse ", e);
                            handleCallback();
                        } finally {
                            if(response != null){
                                response.close();
                            }
                        }
                    }
                });
            } catch (Exception e){
                Log.e(AppUtils.APP_TAG, "BalanceLoaderManager getEtherPrice exception", e);
                handleCallback();
            }
        }

        private void requestBalance(final String address){
            try{
                EtherscanAPI.getInstance().getBalances(address, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onFailure address = " + address, e);
                        removeCallback();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try{
                            ResponseBody body = response.body();
                            if(body != null){
                                //1.parse
                                BalanceResultBean balanceResultBean = new Gson().fromJson(body.string(), BalanceResultBean.class);
                                Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse message = " + balanceResultBean.getMessage());
                                if (!balanceResultBean.getMessage().equalsIgnoreCase("OK")){
                                    Log.e(AppUtils.APP_TAG,"BalanceLoaderManager requestBalance onResponse NOT OK! Don't save it");
                                    return;
                                }
                                List<BalanceResultBean.ResultBean> userBeanList = balanceResultBean.getResult();

                                //2.insert into db
                                final ArrayList<ContentProviderOperation> rawOperations = new ArrayList<ContentProviderOperation>();
                                for(BalanceResultBean.ResultBean resultBean : userBeanList){
                                    Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse balance = " + resultBean.getBalance());
                                    Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse account = " + resultBean.getAccount());
                                    final ContentProviderOperation.Builder updateBuilder = ContentProviderOperation
                                            .newUpdate(XWalletProvider.CONTENT_URI);
                                    updateBuilder.withSelection(DbUtils.ADDRESS_SELECTION, new String[] {resultBean.getAccount()});
                                    updateBuilder.withValue(DbUtils.DbColumns.BALANCE, resultBean.getBalance());
                                    rawOperations.add(updateBuilder.build());
                                }
                                try{
                                    mContext.getContentResolver().applyBatch(XWalletProvider.AUTHORITY, rawOperations);
                                }catch (Exception e){
                                    Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse exception1", e);
                                }
                            }
                        } catch (Exception e){
                            Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse exception2 address = " + address, e);
                        } finally {
                            if(response != null){
                                response.close();
                            }
                            handleCallback();
                        }
                    }
                });
            } catch (Exception e){
                Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse exception3 address = " + address, e);
                handleCallback();
            }
        }

        private void requestBalanceForToken(final String address){
            Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken address = " + address);
            try{
                EtherscanAPI.getInstance().getTokenBalances(address, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken onFailure address = " + address, e);
                        removeCallback();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try{
                            ResponseBody body = response.body();
                            if(body != null){
                                String result = body.string();
                                Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken result = " + result);

                                //1.parse
                                List<TokenListBean.TokenBean> tokens = RetrofitClient.parseTokenJson(result).getTokens();
                                if(tokens == null || tokens.size() <= 0){
                                    Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken no tokens");
                                    return;
                                }

                                ArrayList<ContentProviderOperation> rawOperations = new ArrayList<ContentProviderOperation>();
                                RetrofitClient.updateTokensBalance(rawOperations, tokens, address);
                                if (rawOperations.size() > 0) {
                                    XWalletApplication.getApplication().getContentResolver().applyBatch(XWalletProvider.AUTHORITY, rawOperations);
                                }
                            }
                        } catch (Exception e){
                            Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken onResponse exception1 address = " + address, e);
                        } finally {
                            if(response != null){
                                response.close();
                            }
                            removeCallback();
                        }
                    }
                }, true);
            } catch (Exception e){
                Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken exception2", e);
                removeCallback();
            }
        }
    }

    public static class BalanceLoaded {
        public final String mAddress;
        public final String mBalance;
        public final ArrayList<Double> mBalanceList;
        public final ArrayList<Double> mRateList;

        public BalanceLoaded(String address, String balance, ArrayList list, ArrayList rateList) {
            mAddress = address;
            mBalance = balance;
            mBalanceList = list;
            mRateList = rateList;
        }
    }

    @Override
    public String getTag() {
        return AppUtils.APP_TAG;
    }
}
