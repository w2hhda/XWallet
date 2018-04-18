package com.x.wallet.lib.eth.api;

import com.x.wallet.lib.eth.util.Key;
import com.x.wallet.lib.eth.util.CacheHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class EtherscanAPI {

    private String token;

    private static EtherscanAPI instance;
    public static final String API_KEY = "2R454NXBUUDY673R2YA8S9Z25YDA3RNBXM";

    public static EtherscanAPI getInstance() {
        if (instance == null)
            synchronized (EtherscanAPI.class){
                if (instance == null){
                    instance = new EtherscanAPI();
                }
            }
        return instance;
    }

    public void getPriceChart(long starttime, int period, boolean usd, Callback b) throws IOException {
        get("http://poloniex.com/public?command=returnChartData&currencyPair=" + (usd ? "USDT_ETH" : "BTC_ETH") + "&start=" + starttime + "&end=9999999999&period=" + period, b);
    }


    /**
     * Retrieve all internal transactions from address like contract calls, for normal transactions @see rehanced.com.simpleetherwallet.network.EtherscanAPI#getNormalTransactions() )
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simpleetherwallet.fragments.FragmentTransactions#update() or @see rehanced.com.simpleetherwallet.fragments.FragmentTransactionsAll#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getInternalTransactions(String address, Callback b, boolean force) throws IOException {
        if (!force && CacheHelper.instance().contains(CacheHelper.TYPE_TXS_INTERNAL, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("http://api.etherscan.io/api?module=account&action=txlistinternal&address=" + address + "&startblock=0&endblock=99999999&sort=asc&apikey=" + token)
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), CacheHelper.instance().get(CacheHelper.TYPE_TXS_INTERNAL, address))).build());
            return;
        }
        get("http://api.etherscan.io/api?module=account&action=txlistinternal&address=" + address + "&startblock=0&endblock=99999999&sort=asc&apikey=" + token, b);
    }


    /**
     * Retrieve all normal ether transactions from address (excluding contract calls etc, @see rehanced.com.simpleetherwallet.network.EtherscanAPI#getInternalTransactions() )
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simpleetherwallet.fragments.FragmentTransactions#update() or @see rehanced.com.simpleetherwallet.fragments.FragmentTransactionsAll#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getNormalTransactions(String address, Callback b, boolean force) throws IOException {
        if (!force && CacheHelper.instance().contains(CacheHelper.TYPE_TXS_NORMAL, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("http://api.etherscan.io/api?module=account&action=txlist&address=" + address + "&startblock=0&endblock=99999999&sort=asc&apikey=" + token)
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), CacheHelper.instance().get(CacheHelper.TYPE_TXS_NORMAL, address))).build());
            return;
        }
        get("http://api.etherscan.io/api?module=account&action=txlist&address=" + address + "&startblock=0&endblock=99999999&sort=desc&apikey=" + token, b);
    }


    public void getEtherPrice(Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=stats&action=ethprice&apikey=" + token, b);
    }


    public void getGasPrice(Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=proxy&action=eth_gasPrice&apikey=" + token, b);
    }


    /**
     * Get token balances via ethplorer.io
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simpleetherwallet.fragments.FragmentDetailOverview#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getTokenBalances(String address, Callback b, boolean force) throws IOException {
        if (!force && CacheHelper.instance().contains(CacheHelper.TYPE_TOKEN, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("https://api.ethplorer.io/getAddressInfo/" + address + "?apiKey=freekey")
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), CacheHelper.instance().get(CacheHelper.TYPE_TOKEN, address))).build());
            return;
        }
        get("http://api.ethplorer.io/getAddressInfo/" + address + "?apiKey=freekey", b);
    }


    public void getGasLimitEstimate(String to, Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=proxy&action=eth_estimateGas&to=" + to + "&value=0xff22&gasPrice=0x051da038cc&gas=0xffffff&apikey=" + token, b);
    }


    public void getBalance(String address, Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=account&action=balance&address=" + address + "&apikey=" + token, b);
    }


    public void getNonceForAddress(String address, Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=proxy&action=eth_getTransactionCount&address=" + address + "&tag=latest&apikey=" + token, b);
    }


    public void getPriceConversionRates(String currencyConversion, Callback b) throws IOException {
        get("https://api.fixer.io/latest?base=USD&symbols=" + currencyConversion, b);
    }


    public void getBalances(ArrayList<String> addresses, Callback b) throws IOException {
        String url = "http://api.etherscan.io/api?module=account&action=balancemulti&address=";
        for (String address : addresses)
            url += address + ",";
        url = url.substring(0, url.length() - 1) + "&tag=latest&apikey=" + token; // remove last , AND add token
        get(url, b);
    }

    public void getBalances(String addresses, Callback b) throws IOException {
        String url = "http://api.etherscan.io/api?module=account&action=balancemulti&address=";
        url = url + addresses + "&tag=latest&apikey=" + token; // remove last , AND add token
        get(url, b);
    }

    public void forwardTransaction(String raw, Callback b) throws IOException {
        get("https://api.etherscan.io/api?module=proxy&action=eth_sendRawTransaction&hex=" + raw + "&apikey=" + token, b);
    }

    public void getTokenTxHistory(String address, Callback b) throws  IOException {
        String url = "http://api.etherscan.io/api?module=account&action=tokentx&address=";
        url = url + address + "&startblock=0&endblock=999999999&sort=desc&apikey=" + token;
        get(url, b);
    }

    public void getLastTxHistory(String address, long fromBlockNumber, Callback b) throws  IOException{
        String url = "https://api.etherscan.io/api?module=account&action=txlist&address=" + address +
                "&startblock=" + fromBlockNumber +
                "&endblock=99999999&page=1&offset=50&sort=desc&apikey=" + token;
        get(url, b);
    }

    public void getLastTokenTxHistory(String address, String contractAddress, Callback b) throws IOException {
        String url = "https://api.etherscan.io/api?module=account&action=tokentx&contractaddress=" +
                contractAddress +
                "&address=" +
                address +
                "&page=1&offset=50&sort=desc&apikey=" + token;
        get(url, b);
    }
    

    public void get(String url, Callback b) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(b);
    }


    private EtherscanAPI() {
        token = new Key(API_KEY).toString();
    }

}
