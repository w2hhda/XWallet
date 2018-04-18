package com.x.wallet.transaction.balance;

import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.BalanceResultBean;
import com.x.wallet.lib.eth.data.PriceResultBean;
import com.x.wallet.transaction.token.TokenDeserializer;

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

import static com.x.wallet.db.DbUtils.queryAllEthAddress;

/**
 * Created by wuliang on 18-4-4.
 */

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static Retrofit retrofitEth = null;
    private static Retrofit retrofitToken = null;

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

    private static final String BALANCE_BASE_URL = "http://api.etherscan.io/";
    private static final String TOKEN_BASE_URL = "https://api.ethplorer.io/";

    private static RetrofitEthService getEthService() {
        return RetrofitClient.getEthClient(BALANCE_BASE_URL).create(RetrofitEthService.class);
    }

    private static RetrofitTokenService getTokenService() {
        return RetrofitClient.getTokenClient(TOKEN_BASE_URL).create(RetrofitTokenService.class);
    }

    public static void requestBalance(final OnRequestFinishedListener listener) {
        String addresses = queryAllEthAddress();
        if (TextUtils.isEmpty(addresses)) {
            handleListener(listener);
            Log.i(TAG, "RetrofitClient requestBalance addresses is blank.");
            return;
        }

        Observable<ResponseBody> ethPriceObservable = createPriceResultBeanObservable();
        Observable<ResponseBody> balanceResultBeanObservable = createBalanceResultBeanObservable(addresses);
        ArrayList<Observable<ResponseBody>> tokenBalanceObservableList = createTokenObservableList();

        List<Observable<ResponseBody>> list = new ArrayList<>();
        list.add(ethPriceObservable);
        list.add(balanceResultBeanObservable);
        Log.i(TAG, "RetrofitClient requestBalance tokenBalanceObservableList.size = " + tokenBalanceObservableList.size());
        if (tokenBalanceObservableList.size() > 0) {
            list.addAll(tokenBalanceObservableList);
        }
        Log.i(TAG, "RetrofitClient requestBalance size =" + list.size());

        Observable combind = Observable.zip(list, new FuncN<Object>() {
            @Override
            public Object call(Object... args) {
                if (args == null || args.length <= 0) return null;

                Log.i(TAG, "RetrofitClient requestBalance args.lenght =" + args.length);

                ArrayList<ContentProviderOperation> rawOperations = new ArrayList<ContentProviderOperation>();
                handleObject1(args);
                handleObject2(args, rawOperations);
                if (args.length >= 3) {
                    handleObject3(args, rawOperations);
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

    private static void handleObject1(Object[] args) {
        try {
            Object object = args[0];
            if (object instanceof ResponseBody) {
                ResponseBody responseBody = (ResponseBody) object;
                PriceResultBean priceResultBean = new Gson().fromJson(responseBody.string(), PriceResultBean.class);
                handlePriceResultBean(priceResultBean);
                Log.i(TAG, "RetrofitClient handleObject1 EthToUsd = " + priceResultBean.getResult().getEthusd());
            }
        } catch (Exception e) {
            Log.i(TAG, "RetrofitClient handleObject1 exception", e);
        }
    }

    private static void handleObject2(Object[] args, ArrayList<ContentProviderOperation> rawOperations) {
        try {
            Object object = args[1];
            if (object instanceof ResponseBody) {
                ResponseBody responseBody = (ResponseBody) object;
                BalanceResultBean balanceResultBean = new Gson().fromJson(responseBody.string(), BalanceResultBean.class);
                handleBalanceResultBean(rawOperations, balanceResultBean);
            }
        } catch (Exception e) {
            Log.i(TAG, "RetrofitClient handleObject2 exception", e);
        }
    }

    private static void handleObject3(Object[] args, ArrayList<ContentProviderOperation> rawOperations) {
        try {
            int length = args.length;
            for (int i = 2; i < length; i++) {
                Object object = args[i];
                if (object instanceof ResponseBody) {
                    ResponseBody responseBody = (ResponseBody) object;
                    TokenListBean tokenListBean = parseTokenJson(responseBody.string());
                    handleTokenListBean(rawOperations, tokenListBean);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "RetrofitClient handleObject3 exception", e);
        }
    }

    private static ArrayList<Observable<ResponseBody>> createTokenObservableList() {
        ArrayList<Observable<ResponseBody>> list = new ArrayList();
        Cursor cursor = null;
        try {
            //1.query from db
            cursor = DbUtils.queryAllTokenAddress();
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
            updateBuilder.withSelection(DbUtils.DbColumns.ADDRESS + " = ?", new String[]{resultBean.getAccount()});
            updateBuilder.withValue(DbUtils.DbColumns.BALANCE, resultBean.getBalance());
            rawOperations.add(updateBuilder.build());
        }
    }

    private static void handleTokenListBean(ArrayList<ContentProviderOperation> rawOperations, TokenListBean tokenListBean) {
        List<TokenListBean.TokenBean> tokens = tokenListBean.getTokens();
        if (tokens == null || tokens.size() <= 0) {
            Log.i(TAG, "RetrofitClient handleTokenListBean tokens is blank.");
            return;
        }
        String address = tokenListBean.getAddress();
        for (TokenListBean.TokenBean tokenBean : tokens) {
            Log.i(TAG, "RetrofitClient handleTokenListBean address = " + address + ", balance = " + tokenBean.getBalance());

            TokenListBean.TokenInfo tokenInfo = tokenBean.getTokenInfo();
            String symbol = null;
            int decimals = 1;
            double rate = 0;
            if(tokenInfo != null){
                symbol = tokenInfo.getSymbol();
                decimals = tokenInfo.getDecimals();
                if(tokenInfo.getPrice() != null){
                    rate = tokenInfo.getPrice().getRate();
                }
            }

            String rawBalance = tokenBean.getBalance();
            final ContentProviderOperation.Builder updateBuilder = ContentProviderOperation
                    .newUpdate(XWalletProvider.CONTENT_URI_TOKEN);
            updateBuilder.withSelection(DbUtils.UPDATE_TOKEN_SELECTION, new String[]{address, symbol});
            updateBuilder.withValue(DbUtils.TokenTableColumns.BALANCE, rawBalance);
            updateBuilder.withValue(DbUtils.TokenTableColumns.RATE, rate);
            updateBuilder.withValue(DbUtils.TokenTableColumns.DECIMALS, decimals);
            rawOperations.add(updateBuilder.build());
        }
    }

    private static void handleListener(OnRequestFinishedListener listener) {
        if (listener != null) {
            listener.onRequestFinished();
        }
    }

    public static TokenListBean parseTokenJson(String responseResult){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TokenListBean.class, new TokenDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(responseResult, TokenListBean.class);
    }

    public interface OnRequestFinishedListener {
        void onRequestFinished();
    }
}
