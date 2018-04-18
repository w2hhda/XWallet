package com.x.wallet.lib.btc;

import net.bither.bitherj.api.http.HttpsGetResponse;

public class BlockcypherMytransactionsApi extends HttpsGetResponse<String> {
    public BlockcypherMytransactionsApi(String url) {
        setUrl(url);
    }

    @Override
    public void setResult(String response) throws Exception {
        System.out.println("BlockcypherMytransactionsApi  result = " + response);
        this.result = response;
    }
}
