package com.x.wallet.transaction.balance;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by wuliang on 18-4-4.
 */

public interface RetrofitBtcService {

    //https://blockchain.info/ticker
    @GET("/ticker")
    Observable<ResponseBody> getBtcPrice();

    @GET("latest")
    Call<ResponseBody> getBtcPriceToCall();
}


