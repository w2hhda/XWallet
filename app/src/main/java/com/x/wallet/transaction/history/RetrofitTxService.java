package com.x.wallet.transaction.history;

import com.x.wallet.lib.eth.data.TokenResultBean;
import com.x.wallet.lib.eth.data.TransactionsResultBean;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by wuliang on 18-4-4.
 */

public interface RetrofitTxService {
    //https://api.etherscan.io/api?module=account&action=txlist&address=0xeb574cd5a407fefa5610fcde6aec13d983ba527c&startblock=0&endblock=99999999&page=1&offset=10&sort=desc&apikey=JG634TZC90MJLKWO9JS1ZXGFAOPF89K2M2
    ///api?module=stats&action=ethprice&apikey=" + token
    @GET("/api")
    Call<TransactionsResultBean> getTxList(@Query("module") String module,  //account
                                           @Query("action") String action, //txlist
                                           @Query("address") String address,//address
                                           @Query("startblock") String startblock, //startblock
                                           @Query("endblock") String endblock, //endblock
                                           @Query("page") int page, //page
                                           @Query("offset") int offset, //offset
                                           @Query("sort") String sort, //sort
                                           @Query("apikey") String apiKey); //apikey

    //https://api.etherscan.io/api?module=account&action=tokentx&contractaddress=0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2&address=0x4e83362442b8d1bec281594cea3050c8eb01311c&page=1&offset=100&sort=asc&apikey=YourApiKeyToken
    @GET("/api")
    Call<TokenResultBean> getTokenTxList(@Query("module") String module,  //account
                                         @Query("action") String action, //tokentx
                                         @Query("contractaddress") String contractaddress,//contractaddress
                                         @Query("address") String address,//address
                                         @Query("page") long page, //page
                                         @Query("offset") int offset, //offset
                                         @Query("sort") String sort, //sort
                                         @Query("apikey") String apiKey); //apikey
}


