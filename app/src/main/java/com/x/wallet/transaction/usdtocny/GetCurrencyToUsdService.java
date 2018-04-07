package com.x.wallet.transaction.usdtocny;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by admin on 2018/4/6.
 */

public interface GetCurrencyToUsdService {
    //https://api.fixer.io/latest?base=USD&symbols=
    //https://data.fixer.io/api/
    @GET("latest")
    Call<ResponseBody> getCurrencyToUsd(@Query("base") String base, @Query("symbols") String symbols);
}