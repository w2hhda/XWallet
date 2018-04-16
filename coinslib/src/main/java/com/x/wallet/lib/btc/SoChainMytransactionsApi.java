package com.x.wallet.lib.btc;

import net.bither.bitherj.api.http.HttpsGetResponse;

public class SoChainMytransactionsApi extends HttpsGetResponse<String> {
    public SoChainMytransactionsApi(String url) {
        setUrl(url);
    }

    @Override
    public void setResult(String response) throws Exception {
        System.out.println("SoChainMytransactionsApi  result = " + response);
        this.result = response;
    }
}
