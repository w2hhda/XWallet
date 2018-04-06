package com.x.wallet.transaction.balance;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by wuliang on 18-4-4.
 */

public interface RetrofitTokenService {

    //https://api.ethplorer.io/
    //getAddressInfo/{address}?apiKey=freekey
    @GET("/getAddressInfo/{address}")
    Observable<ResponseBody> getTokenList(@Path("address") String address, @Query("apiKey") String apiKey);
}


