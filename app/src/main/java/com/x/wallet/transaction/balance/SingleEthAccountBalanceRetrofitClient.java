package com.x.wallet.transaction.balance;

import android.util.Log;

import com.x.wallet.db.DbUtils;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.EthBalanceResultBean;
import com.x.wallet.lib.eth.data.PriceResultBean;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by wuliang on 18-4-4.
 */

public class SingleEthAccountBalanceRetrofitClient {
    private static final String TAG = "SingleEabRetrofitClient";

    private static Retrofit retrofitEth = null;

    public static void requestBalance(final OnRequestFinishedListener listener, final String accountAddress) {
        Observable<PriceResultBean> ethPriceObservable = createPriceResultBeanObservable();
        Observable<EthBalanceResultBean> balanceResultBeanObservable = createBalanceResultBeanObservable(accountAddress);

        Observable combined = Observable.zip(ethPriceObservable, balanceResultBeanObservable, new Func2<PriceResultBean, EthBalanceResultBean, String>() {
            @Override
            public String call(PriceResultBean priceResultBean, EthBalanceResultBean balanceResultBean) {
                handlePriceResultBean(priceResultBean);
                handleBalanceResultBean(accountAddress, balanceResultBean);
                return null;
            }
        });
        combined.subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "SingleEthAccountBalanceRetrofitClient onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "SingleEthAccountBalanceRetrofitClient onError", e);
                handleListener(listener);
            }

            @Override
            public void onNext(Object result) {
                Log.i(TAG, "SingleEthAccountBalanceRetrofitClient onNext result = " + result);
                handleListener(listener);
            }
        });
    }

    private static Retrofit getEthClient(String baseUrl) {
        if (retrofitEth == null) {
            retrofitEth = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitEth;
    }

    private static RetrofitEthService getEthService() {
        return getEthClient(RetrofitClient.BALANCE_BASE_URL).create(RetrofitEthService.class);
    }

    private static Observable<PriceResultBean> createPriceResultBeanObservable() {
        return getEthService().getEthPriceToCall("stats", "ethprice", EtherscanAPI.API_KEY).subscribeOn(Schedulers.newThread());
    }

    private static Observable<EthBalanceResultBean> createBalanceResultBeanObservable(String addresses) {
        return getEthService().getBalance("account", "balance", addresses, "latest", EtherscanAPI.API_KEY).subscribeOn(Schedulers.newThread());
    }

    private static void handlePriceResultBean(PriceResultBean priceResultBean) {
        BalanceConversionUtils.write(priceResultBean.getResult().getEthusd());
    }

    private static void handleBalanceResultBean(String accountAddress, EthBalanceResultBean balanceResultBean) {
        String message = balanceResultBean.getMessage();
        if(message.equals("OK")){
            DbUtils.updateAccountBalance(accountAddress, balanceResultBean.getResult());
        }
    }

    private static void handleListener(OnRequestFinishedListener listener) {
        if (listener != null) {
            listener.onRequestFinished();
        }
    }

    public interface OnRequestFinishedListener {
        void onRequestFinished();
    }
}
