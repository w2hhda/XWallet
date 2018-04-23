package com.x.wallet.transaction.balance;

import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.btc.BtcUtils;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.BalanceResultBean;
import com.x.wallet.lib.eth.data.PriceResultBean;
import com.x.wallet.transaction.token.TokenDeserializer;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

/**
 * Created by wuliang on 18-4-4.
 */

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static Retrofit retrofitEth = null;
    private static Retrofit retrofitToken = null;
    private static Retrofit mRetrofitBtc = null;

    private static Retrofit getEthClient(String baseUrl) {
        if (retrofitEth == null) {
            /*HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(logInterceptor).build();*/
            retrofitEth = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    // .client(okHttpClient)
                    .build();
        }
        return retrofitEth;
    }

    private static Retrofit getTokenClient(String baseUrl) {
        if (retrofitToken == null) {
            retrofitToken = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitToken;
    }

    private static Retrofit getRetrofitBtc(String baseUrl) {
        if (mRetrofitBtc == null) {
            mRetrofitBtc = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mRetrofitBtc;
    }

    private static final String BALANCE_BASE_URL = "https://api.etherscan.io/";
    private static final String TOKEN_BASE_URL = "https://api.ethplorer.io/";
    private static final String BTC_BASE_URL = "https://blockchain.info/";

    private static RetrofitEthService getEthService() {
        return RetrofitClient.getEthClient(BALANCE_BASE_URL).create(RetrofitEthService.class);
    }

    private static RetrofitTokenService getTokenService() {
        return RetrofitClient.getTokenClient(TOKEN_BASE_URL).create(RetrofitTokenService.class);
    }

    public static RetrofitBtcService getBtcService(){
        return RetrofitClient.getRetrofitBtc(BTC_BASE_URL).create(RetrofitBtcService.class);
    }

    public static void requestBalance(final OnRequestFinishedListener listener, final boolean isNeedAddTokenAutomatic) {
        String addresses = DbUtils.queryAllEthAddress();
        boolean noEthAccount = TextUtils.isEmpty(addresses);
        boolean hasBtcAccount = DbUtils.hasBtcAccount();
        if (noEthAccount && !hasBtcAccount) {
            handleListener(listener);
            Log.i(TAG, "RetrofitClient requestBalance addresses is blank.");
            return;
        }
        List<Observable<ResponseBody>> list = new ArrayList<>();
        if(!noEthAccount){
            Observable<ResponseBody> ethPriceObservable = createPriceResultBeanObservable();
            Observable<ResponseBody> balanceResultBeanObservable = createBalanceResultBeanObservable(addresses);
            ArrayList<Observable<ResponseBody>> tokenBalanceObservableList = createTokenObservableList(isNeedAddTokenAutomatic);
            list.add(ethPriceObservable);
            list.add(balanceResultBeanObservable);
            Log.i(TAG, "RetrofitClient requestBalance tokenBalanceObservableList.size = " + tokenBalanceObservableList.size());
            if (tokenBalanceObservableList.size() > 0) {
                list.addAll(tokenBalanceObservableList);
            }
        }

        if(hasBtcAccount){
            Observable<ResponseBody> btcPriceObservable = createBtcPriceResultBeanObservable();
            list.add(btcPriceObservable);
        }

        Log.i(TAG, "RetrofitClient requestBalance size =" + list.size());

        Observable combind = Observable.zip(list, new FuncN<Object>() {
            @Override
            public Object call(Object... args) {
                if (args == null || args.length <= 0) return null;

                Log.i(TAG, "RetrofitClient requestBalance args.lenght =" + args.length);

                ArrayList<ContentProviderOperation> rawOperations = new ArrayList<ContentProviderOperation>();
                for(Object object : args){
                    handleObject(object, rawOperations, isNeedAddTokenAutomatic);
                }
                Log.i(TAG, "RetrofitClient requestBalance rawOperations.size = " + rawOperations.size());
                try {
                    if (rawOperations.size() > 0) {
                        XWalletApplication.getApplication().getContentResolver().applyBatch(XWalletProvider.AUTHORITY, rawOperations);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "RetrofitClient requestBalance exception", e);
                }
                return "SUCCESS";
            }
        });
        combind.subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "RetrofitClient onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "RetrofitClient onError", e);
                handleListener(listener);
            }

            @Override
            public void onNext(Object result) {
                Log.i(TAG, "RetrofitClient onNext result = " + result);
                handleListener(listener);
            }
        });
    }

    private static void handleObject(Object object, ArrayList<ContentProviderOperation> rawOperations, boolean isNeedAddTokenAutomatic){
        try {
            if (object instanceof ResponseBody) {
                ResponseBody responseBody = (ResponseBody) object;
                String result = responseBody.string();
                JSONObject jsonObject = new JSONObject(result);
                if(jsonObject.has("status")){
                    if(jsonObject.has("result")){
                        Object resultObject = jsonObject.get("result");
                        if(resultObject instanceof JSONObject){
                            JSONObject resultJsonObject = (JSONObject)resultObject;
                            if(resultJsonObject.has("ethusd")){
                                handleEthPriceJson(result);
                            }
                        } else if(resultObject instanceof JSONArray){
                            handleEthBalanceJson(result, rawOperations);
                        }
                    }

                } else if(jsonObject.has("address")){
                    handleTokenBalanceJson(result, rawOperations, isNeedAddTokenAutomatic);
                } else if(jsonObject.has("USD")){
                    BtcUtils.handleBtcPriceJson(jsonObject, UsdToCnyHelper.mCurrentCurrency);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "RetrofitClient handleObject exception", e);
        }
    }

    private static void handleEthPriceJson(String result) {
        try {
            PriceResultBean priceResultBean = new Gson().fromJson(result, PriceResultBean.class);
            handlePriceResultBean(priceResultBean);
            Log.i(TAG, "RetrofitClient handleEthPriceJson EthToUsd = " + priceResultBean.getResult().getEthusd());
        } catch (Exception e) {
            Log.i(TAG, "RetrofitClient handleEthPriceJson exception", e);
        }
    }

    private static void handleEthBalanceJson(String result, ArrayList<ContentProviderOperation> rawOperations) {
        try {
            BalanceResultBean balanceResultBean = new Gson().fromJson(result, BalanceResultBean.class);
            handleBalanceResultBean(rawOperations, balanceResultBean);
        } catch (Exception e) {
            Log.i(TAG, "RetrofitClient handleEthBalanceJson exception", e);
        }
    }

    private static void handleTokenBalanceJson(String result, ArrayList<ContentProviderOperation> rawOperations, boolean isNeedAddTokenAutomatic) {
        try {
            TokenListBean tokenListBean = parseTokenJson(result);
            handleTokenListBean(rawOperations, tokenListBean, isNeedAddTokenAutomatic);
        } catch (Exception e) {
            Log.i(TAG, "RetrofitClient handleTokenBalanceJson exception", e);
        }
    }

    private static ArrayList<Observable<ResponseBody>> createTokenObservableList(boolean isNeedAddTokenAutomatic) {
        ArrayList<Observable<ResponseBody>> list = new ArrayList();
        Cursor cursor = null;
        try {
            if (isNeedAddTokenAutomatic) {
                cursor = DbUtils.queryAllEthAddressToCursor();
            } else {
                //1.query from db
                cursor = DbUtils.queryAllTokenAddress();
            }
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String accountAddress = cursor.getString(0);
                    Log.i(TAG, "RetrofitClient createTokenObservableList accountAddress = " + accountAddress);
                    if (!TextUtils.isEmpty(accountAddress)) {
                        Observable<ResponseBody> observable = createTokenObservable(accountAddress);
                        if (observable != null) {
                            list.add(observable);
                        }
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    private static Observable<ResponseBody> createTokenObservable(String accountAddress) {
        return getTokenService().getTokenList(accountAddress, "freekey").subscribeOn(Schedulers.newThread());
    }

    private static Observable<ResponseBody> createPriceResultBeanObservable() {
        return getEthService().getEthPrice("stats", "ethprice", EtherscanAPI.API_KEY).subscribeOn(Schedulers.newThread());
    }

    private static Observable<ResponseBody> createBalanceResultBeanObservable(String addresses) {
        return getEthService().getListBalance("account", "balancemulti", addresses, "latest", EtherscanAPI.API_KEY).subscribeOn(Schedulers.newThread());
    }

    private static Observable<ResponseBody> createBtcPriceResultBeanObservable(){
        return getBtcService().getBtcPrice().subscribeOn(Schedulers.newThread());
    }

    private static void handlePriceResultBean(PriceResultBean priceResultBean) {
        BalanceConversionUtils.write(priceResultBean.getResult().getEthusd());
    }

    private static void handleBalanceResultBean(ArrayList<ContentProviderOperation> rawOperations, BalanceResultBean balanceResultBean) {
        List<BalanceResultBean.ResultBean> userBeanList = balanceResultBean.getResult();
        if (userBeanList == null || userBeanList.size() <= 0) {
            Log.i(TAG, "RetrofitClient handleBalanceResultBean userBeanList is blank.");
            return;
        }

        //2.insert into db
        for (BalanceResultBean.ResultBean resultBean : userBeanList) {
            Log.i(TAG, "RetrofitClient handleBalanceResultBean balance = " + resultBean.getBalance() + ", account = " + resultBean.getAccount());
            final ContentProviderOperation.Builder updateBuilder = ContentProviderOperation
                    .newUpdate(XWalletProvider.CONTENT_URI);
            updateBuilder.withSelection(DbUtils.ADDRESS_SELECTION, new String[]{resultBean.getAccount()});
            updateBuilder.withValue(DbUtils.DbColumns.BALANCE, resultBean.getBalance());
            rawOperations.add(updateBuilder.build());
        }
    }

    private static void handleTokenListBean(ArrayList<ContentProviderOperation> rawOperations, TokenListBean tokenListBean, boolean isNeedAddTokenAutomatic) {
        List<TokenListBean.TokenBean> tokens = tokenListBean.getTokens();
        if (tokens == null || tokens.size() <= 0) {
            Log.i(TAG, "RetrofitClient handleTokenListBean tokens is blank.");
            return;
        }
        if(isNeedAddTokenAutomatic){
            handleAutoAddToken(rawOperations, tokens, tokenListBean.getAddress());
        } else {
            String address = tokenListBean.getAddress();
            for (TokenListBean.TokenBean tokenBean : tokens) {
                Log.i(TAG, "RetrofitClient handleTokenListBean address = " + address + ", balance = " + tokenBean.getBalance());

                TokenListBean.TokenInfo tokenInfo = tokenBean.getTokenInfo();
                String symbol = null;
                int decimals = 1;
                double rate = 0;
                if (tokenInfo != null) {
                    symbol = tokenInfo.getSymbol();
                    decimals = tokenInfo.getDecimals();
                    if (tokenInfo.getPrice() != null) {
                        rate = tokenInfo.getPrice().getRate();
                    }
                }

                rawOperations.add(buildUpdateTokenOperation(address, symbol, tokenBean.getBalance(), rate, decimals));
            }
        }
    }

    private static void handleAutoAddToken(ArrayList<ContentProviderOperation> rawOperations, List<TokenListBean.TokenBean> tokens, String address) {
        long accountId = DbUtils.queryEthAccountId(address);

        for (TokenListBean.TokenBean tokenBean : tokens) {
            boolean isExist = false;
            TokenListBean.TokenInfo tokenInfo = tokenBean.getTokenInfo();
            String symbol = null;
            int decimals = 1;
            double rate = 0;
            String tokeName= "";
            String contractAddress = "";
            if (tokenInfo != null) {
                contractAddress = tokenInfo.getAddress();
                symbol = tokenInfo.getSymbol();
                decimals = tokenInfo.getDecimals();
                if (tokenInfo.getPrice() != null) {
                    rate = tokenInfo.getPrice().getRate();
                }
                tokeName = tokenInfo.getName();
                isExist = DbUtils.isAlreadyExistToken(DbUtils.UPDATE_TOKEN_SELECTION, new String[]{address, symbol});
            }
            if(isExist){
                rawOperations.add(buildUpdateTokenOperation(address, symbol, tokenBean.getBalance(), rate, decimals));
            } else {
                boolean hasDeleted = AppUtils.hasDeleted(address, tokeName);
                if(!hasDeleted){
                    rawOperations.add(buildInsertTokenOperation(accountId, address, tokens.indexOf(tokenBean),
                            tokeName, symbol, decimals,
                            contractAddress, tokenBean.getBalance(), String.valueOf(rate)));
                }
            }
        }
    }

    private static void handleListener(OnRequestFinishedListener listener) {
        if (listener != null) {
            listener.onRequestFinished();
        }
    }

    public static TokenListBean parseTokenJson(String responseResult) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TokenListBean.class, new TokenDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(responseResult, TokenListBean.class);
    }

    private static ContentProviderOperation buildInsertTokenOperation(long accountId, String accountAddress, int idInAll,
                                                                      String name, String symbol, int decimals,
                                                                      String contractAddress, String rawBalance, String rate) {
        final ContentProviderOperation.Builder insertBuilder = ContentProviderOperation
                .newInsert(XWalletProvider.CONTENT_URI_TOKEN);
        insertBuilder.withValue(DbUtils.TokenTableColumns.ACCOUNT_ID, accountId);
        insertBuilder.withValue(DbUtils.TokenTableColumns.ACCOUNT_ADDRESS, accountAddress);
        insertBuilder.withValue(DbUtils.TokenTableColumns.ID_IN_ALL, idInAll);
        insertBuilder.withValue(DbUtils.TokenTableColumns.NAME, name);
        insertBuilder.withValue(DbUtils.TokenTableColumns.SYMBOL, symbol);
        insertBuilder.withValue(DbUtils.TokenTableColumns.DECIMALS, decimals);
        insertBuilder.withValue(DbUtils.TokenTableColumns.CONTRACT_ADDRESS, contractAddress);
        insertBuilder.withValue(DbUtils.TokenTableColumns.BALANCE, rawBalance);
        insertBuilder.withValue(DbUtils.TokenTableColumns.RATE, rate);
        return insertBuilder.build();
    }

    private static ContentProviderOperation buildUpdateTokenOperation(String address, String symbol,
                                                                      String rawBalance, double rate, int decimals) {
        final ContentProviderOperation.Builder updateBuilder = ContentProviderOperation
                .newUpdate(XWalletProvider.CONTENT_URI_TOKEN);
        updateBuilder.withSelection(DbUtils.UPDATE_TOKEN_SELECTION, new String[]{address, symbol});
        updateBuilder.withValue(DbUtils.TokenTableColumns.BALANCE, rawBalance);
        updateBuilder.withValue(DbUtils.TokenTableColumns.RATE, rate);
        updateBuilder.withValue(DbUtils.TokenTableColumns.DECIMALS, decimals);
        return updateBuilder.build();
    }

    public interface OnRequestFinishedListener {
        void onRequestFinished();
    }
}
