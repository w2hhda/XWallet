package com.x.wallet.transaction.balance;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.x.wallet.AppUtils;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.BalanceResultBean;
import com.x.wallet.lib.eth.data.PriceResultBean;
import com.x.wallet.transaction.token.BackgroundLoaderManager;
import com.x.wallet.transaction.token.TokenUtils;

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
    public static final String ALL_ADDRESS_BALANCE = "all_address_balance";
    public static final String ALL_TOKEN_BALANCE = "all_token_balance";
    private Context mContext;

    public BalanceLoaderManager(Context context) {
        super(context);
        mContext = context;
    }

    public ItemLoadedFuture getBalance(final ItemLoadedCallback<BalanceLoaded> callback) {
        return getBalance(Uri.parse(ALL_ADDRESS_BALANCE), callback);
    }

    public ItemLoadedFuture getAllTokenBalance(final ItemLoadedCallback<BalanceLoaded> callback){
        return getBalance(Uri.parse(ALL_TOKEN_BALANCE), callback);
    }

    public ItemLoadedFuture getBalance(Uri uri, final ItemLoadedCallback<BalanceLoaded> callback) {
        Log.i(AppUtils.APP_TAG, "BalanceLoaderManager getBalance uri = " + uri);
        if (uri == null) {
            return null;
        }

        final boolean taskExists = mPendingTaskUris.contains(uri);
        final boolean callbackRequired = (callback != null);

        if (callbackRequired) {
            addCallback(uri, callback);
        }

        if (!taskExists) {
            mPendingTaskUris.add(uri);
            Log.i(AppUtils.APP_TAG, "BalanceLoaderManager getBalance start task.");
            Runnable task = new BalanceTask(uri);
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

        public BalanceTask(Uri address) {
            mUri = address;
        }

        /** {@inheritDoc} */
        public void run() {
            Log.i(AppUtils.APP_TAG, "BalanceLoaderManager run mUri = " + mUri);
            if(ALL_ADDRESS_BALANCE.equals(mUri.toString())){
                String allEthAddress = queryAllEthAddress();
                if(TextUtils.isEmpty(allEthAddress)){
                    handleCallback();
                    return;
                }
                getEtherPrice(allEthAddress);
            } else if(ALL_TOKEN_BALANCE.equals(mUri.toString())){
                requestBalanceForToken();
            } else {
                String address = mUri.getLastPathSegment();
                if(mUri.toString().startsWith(TokenUtils.QUERY_TOKEN_BALANCE_URI.toString())){
                    requestBalanceForToken(address);
                }
            }
        }

        private String queryAllEthAddress(){
            Cursor cursor = null;
            try{
                //1.query from db
                cursor = mContext.getContentResolver().query(
                        XWalletProvider.CONTENT_URI, new String[]{DbUtils.DbColumns.ADDRESS}, null, null, null);
                if(cursor != null && cursor.getCount() > 0){
                    StringBuilder builder = new StringBuilder("");
                    while (cursor.moveToNext()){
                        builder.append(cursor.getString(0));
                        builder.append(",");
                    }
                    String address = builder.toString();
                    return address.substring(0, address.length() -1);
                }
            } finally {
                if(cursor != null){
                    cursor.close();
                }
            }
            return null;
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
                                List<BalanceResultBean.ResultBean> userBeanList = balanceResultBean.getResult();

                                //2.insert into db
                                final ArrayList<ContentProviderOperation> rawOperations = new ArrayList<ContentProviderOperation>();
                                for(BalanceResultBean.ResultBean resultBean : userBeanList){
                                    Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse balance = " + resultBean.getBalance());
                                    Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse account = " + resultBean.getAccount());
                                    final ContentProviderOperation.Builder updateBuilder = ContentProviderOperation
                                            .newUpdate(XWalletProvider.CONTENT_URI);
                                    updateBuilder.withSelection(DbUtils.DbColumns.ADDRESS + " = ?", new String[] {resultBean.getAccount()});
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

        private void requestBalanceForToken(){
            Cursor cursor = null;
            try{
                //1.query from db
                cursor = mContext.getContentResolver().query(
                        XWalletProvider.CONTENT_URI_TOKEN, new String[]{DbUtils.TokenTableColumns.ACCOUNT_ADDRESS}, null, null, null);
                if(cursor != null && cursor.getCount() > 0){
                    while (cursor.moveToNext()){
                        String accountAddress = cursor.getString(0);
                        Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken accountAddress = " + accountAddress);
                        if(!TextUtils.isEmpty(accountAddress)){
                            requestBalanceForToken(accountAddress);
                        } else {
                            removeCallback();
                        }
                    }
                } else {
                    removeCallback();
                }
            } finally {
                if(cursor != null){
                    cursor.close();
                }
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
                                List<TokenListBean.TokenBean> tokens = parseTokenJson(result);
                                if(tokens == null || tokens.size() <= 0){
                                    Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken no tokens");
                                    return;
                                }

                                //2.insert into db
                                updateTokenBalanceIntoDb(address, tokens);
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

        private List<TokenListBean.TokenBean> parseTokenJson(String result){
            TokenListBean tokenListBean = new Gson().fromJson(result, TokenListBean.class);
            return tokenListBean.getTokens();
        }

        private void updateTokenBalanceIntoDb(String address, List<TokenListBean.TokenBean> tokens){
            final ArrayList<ContentProviderOperation> rawOperations = new ArrayList<ContentProviderOperation>();
            for(TokenListBean.TokenBean tokenBean : tokens){
                Log.i(AppUtils.APP_TAG, "BalanceLoaderManager updateTokenBalanceIntoDb address = " + address + ", balance = " + tokenBean.getBalance());

                String symbol = tokenBean.getTokenInfo().getSymbol();
                String rawBalance = tokenBean.getBalance();
                double rate = tokenBean.getTokenInfo().getPrice().getRate();
                int decimals = tokenBean.getTokenInfo().getDecimals();

                final ContentProviderOperation.Builder updateBuilder = ContentProviderOperation
                        .newUpdate(XWalletProvider.CONTENT_URI_TOKEN);
                updateBuilder.withSelection(DbUtils.UPDATE_TOKEN_SELECTION, new String[] {address, symbol});
                updateBuilder.withValue(DbUtils.TokenTableColumns.BALANCE, rawBalance);
                updateBuilder.withValue(DbUtils.TokenTableColumns.RATE, rate);
                updateBuilder.withValue(DbUtils.TokenTableColumns.DECIMALS, decimals);
                rawOperations.add(updateBuilder.build());
            }
            try{
                mContext.getContentResolver().applyBatch(XWalletProvider.AUTHORITY, rawOperations);
            }catch (Exception e){
                Log.e(AppUtils.APP_TAG, "BalanceLoaderManager updateTokenBalanceIntoDb exception", e);
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
