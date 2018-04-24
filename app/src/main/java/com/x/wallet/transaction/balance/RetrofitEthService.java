package com.x.wallet.transaction.balance;

import com.x.wallet.lib.eth.data.EthBalanceResultBean;
import com.x.wallet.lib.eth.data.PriceResultBean;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by wuliang on 18-4-4.
 */

public interface RetrofitEthService {

    //http://api.etherscan.io/
    ///api?module=stats&action=ethprice&apikey=" + token
    @GET("/api")
    Observable<ResponseBody> getEthPrice(@Query("module") String module,  //stats
                                         @Query("action") String action, //ethprice
                                         @Query("apikey") String apiKey); //apikey

    @GET("/api")
    Observable<PriceResultBean> getEthPriceToCall(@Query("module") String module,  //stats
                                      @Query("action") String action, //ethprice
                                      @Query("apikey") String apiKey); //apikey

    //http://api.etherscan.io/
    //api?module=account&action=balancemulti&address={addresses}&tag=latest&apikey={key}
    @GET("/api")
    Observable<ResponseBody> getListBalance(@Query("module") String module,  //account
                                                 @Query("action") String action,  //balancemulti
                                                 @Query("address") String addresses,
                                                 @Query("tag") String tag,        //latest
                                                 @Query("apikey") String apikey);  //apiKey

    //https://api.etherscan.io/api?module=account&action=balance&address=0xddbd2b932c763ba5b1b7ae3b362eac3e8d40121a&tag=latest&apikey=YourApiKeyToken
    @GET("/api")
    Observable<EthBalanceResultBean> getBalance(@Query("module") String module,  //account
                                                @Query("action") String action,  //balance
                                                @Query("address") String addresses,
                                                @Query("tag") String tag,        //latest
                                                @Query("apikey") String apikey);  //apiKey
}


