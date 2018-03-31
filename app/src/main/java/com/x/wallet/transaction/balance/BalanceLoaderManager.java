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
import com.x.wallet.lib.eth.data.UsdCnyBean;
import com.x.wallet.transaction.token.BackgroundLoaderManager;
import com.x.wallet.transaction.token.TokenLoaderManager;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.ui.data.TokenItemBean;
import com.x.wallet.ui.view.TokenListItem;

import java.io.IOException;
import java.math.BigDecimal;
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
    private Context mContext;

    public BalanceLoaderManager(Context context) {
        super(context);
        mContext = context;
    }

    public ItemLoadedFuture getBalance(final ItemLoadedCallback<BalanceLoaded> callback) {
        return getBalance(Uri.parse(ALL_ADDRESS_BALANCE), callback);
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
            if(ALL_ADDRESS_BALANCE.equals(mUri.toString())){
                String allEthAddress = queryAllEthAddress();
                if(TextUtils.isEmpty(allEthAddress)){
                    removeCallback();
                    return;
                }

                requestBalance(allEthAddress);
            } else {
                String address = mUri.getLastPathSegment();
                Log.i(AppUtils.APP_TAG, "BalanceLoaderManager run mUri = " + mUri);
                requestBalanceForToken(address);
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
                                BigDecimal tempAllBalance = BigDecimal.ZERO;
                                final ArrayList<ContentProviderOperation> rawOperations = new ArrayList<ContentProviderOperation>();
                                for(BalanceResultBean.ResultBean resultBean : userBeanList){
                                    Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse balance = " + resultBean.getBalance());
                                    Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse account = " + resultBean.getAccount());
                                    final ContentProviderOperation.Builder updateBuilder = ContentProviderOperation
                                            .newUpdate(XWalletProvider.CONTENT_URI);
                                    updateBuilder.withSelection(DbUtils.DbColumns.ADDRESS + " = ?", new String[] {resultBean.getAccount()});
                                    updateBuilder.withValue(DbUtils.DbColumns.BALANCE, resultBean.getBalance());
                                    rawOperations.add(updateBuilder.build());
                                    tempAllBalance = tempAllBalance.add(new BigDecimal(resultBean.getBalance()));
                                }
                                BalanceConversionUtils.mAllBalance = tempAllBalance;
                                try{
                                    mContext.getContentResolver().applyBatch(XWalletProvider.AUTHORITY, rawOperations);
                                }catch (Exception e){
                                    Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse exception1", e);
                                }
                                if(tempAllBalance.compareTo(BigDecimal.ZERO) == 1){
                                    getEtherPrice();
                                }
                            }
                        } catch (Exception e){
                            Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalance onResponse exception2 address = " + address, e);
                        } finally {
                            if(response != null){
                                response.close();
                            }
                            removeCallback();
                        }
                    }
                });
            } catch (Exception e){
                Log.e(AppUtils.APP_TAG, "BalanceLoaderManager BalanceTask exception", e);
                removeCallback();
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

        private void getEtherPrice(){
            try{
                EtherscanAPI.getInstance().getEtherPrice(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(AppUtils.APP_TAG, "BalanceLoaderManager getEtherPrice onFailure exception", e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try{
                            ResponseBody body = response.body();
                            if(body != null){
                                //1.parse
                                PriceResultBean priceResultBean = new Gson().fromJson(body.string(), PriceResultBean.class);
                                BalanceConversionUtils.mEthToUsd = priceResultBean.getResult().getEthusd();
                                Log.i(AppUtils.APP_TAG, "BalanceLoaderManager onResponse getEtherPrice EthToUsd = " + priceResultBean.getResult().getEthusd());

                                EtherscanAPI.getInstance().getPriceConversionRates("CNY", new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Log.e(AppUtils.APP_TAG, "BalanceLoaderManager onFailure for getPriceConversionRates" , e);
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        ResponseBody body2 = response.body();
                                        if (body2 != null){
                                            UsdCnyBean usdCnyBean = new Gson().fromJson(body2.string(), UsdCnyBean.class);
                                            BalanceConversionUtils.mUsdToCny = usdCnyBean.getRates().getCNY();
                                            BalanceConversionUtils.responseToListener();
                                            BalanceConversionUtils.handleListener();
                                            Log.i(AppUtils.APP_TAG, "BalanceLoaderManager onResponse UsdToCny = " + usdCnyBean.getRates().getCNY());
                                        }
                                    }
                                });
                            }
                        } finally {
                            if(response != null){
                                response.close();
                            }
                        }

                    }
                });
            } catch (Exception e){
                Log.e(AppUtils.APP_TAG, "BalanceLoaderManager getEtherPrice exception", e);
            }
        }

        private void requestBalanceForToken(final String address){
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
                                Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken body = " + result);

                                //1.parse
                                TokenListBean tokenListBean = new Gson().fromJson(result, TokenListBean.class);
                                List<TokenListBean.TokenBean> tokens = tokenListBean.getTokens();
                                if(tokens != null){
                                    Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken tokens.size = " + tokens.size());
                                }

                                //2.insert into db
                                final ArrayList<String> balanceList = new ArrayList<>();
                                final ArrayList<Double> rateList = new ArrayList<>();
                                double tempAllTokenBalance = 0;
                                final ArrayList<ContentProviderOperation> rawOperations = new ArrayList<ContentProviderOperation>();
                                for(TokenListBean.TokenBean tokenBean : tokens){
                                    Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken onResponse balance = " + tokenBean.getBalance());
                                    final ContentProviderOperation.Builder updateBuilder = ContentProviderOperation
                                            .newUpdate(XWalletProvider.CONTENT_URI_TOKEN);
                                    updateBuilder.withSelection(DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " = ? AND " + DbUtils.TokenTableColumns.SYMBOL + " = ?",
                                            new String[] {address, tokenBean.getTokenInfo().getSymbol()});
                                    updateBuilder.withValue(DbUtils.TokenTableColumns.BALANCE, tokenBean.getBalance());
                                    updateBuilder.withValue(DbUtils.TokenTableColumns.RATE, tokenBean.getTokenInfo().getPrice().getRate());
                                    rawOperations.add(updateBuilder.build());
                                    balanceList.add(TokenUtils.translateToken(tokenBean.getBalance(), tokenBean.getTokenInfo().getDecimals()).toString());
                                    rateList.add(tokenBean.getTokenInfo().getPrice().getRate());
                                    tempAllTokenBalance = tempAllTokenBalance + TokenUtils.calculateTokenBalance(tokenBean.getBalance(),
                                            tokenBean.getTokenInfo().getDecimals(), tokenBean.getTokenInfo().getPrice().getRate());
                                }
                                BalanceConversionUtils.mAllTokenBalance = tempAllTokenBalance;
                                try{
                                    mContext.getContentResolver().applyBatch(XWalletProvider.AUTHORITY, rawOperations);
                                }catch (Exception e){
                                    Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken onResponse exception1", e);
                                }

                                mCallbackHandler.post(new Runnable() {
                                    public void run() {
                                        final Set<ItemLoadedCallback> callbacks = mCallbacks.get(mUri);
                                        if (callbacks != null) {
                                            // Make a copy so that the callback can unregister itself
                                            for (final ItemLoadedCallback<BalanceLoaded> callback : asList(callbacks)) {
                                                Log.i(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken onResponse callback");
                                                BalanceLoaded balanceLoaded = new BalanceLoaded(address, null, balanceList, rateList);
                                                callback.onItemLoaded(balanceLoaded, null);
                                            }
                                        }
                                        mCallbacks.remove(mUri);
                                        mPendingTaskUris.remove(mUri);
                                    }
                                });
                            }
                        } catch (Exception e){
                            Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken onResponse exception2 address = " + address, e);
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
        public final ArrayList<String> mBalanceList;
        public final ArrayList<Double> mRateList;

        public BalanceLoaded(String address, String balance, ArrayList<String> list, ArrayList rateList) {
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
