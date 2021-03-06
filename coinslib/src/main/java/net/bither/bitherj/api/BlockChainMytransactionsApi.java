package net.bither.bitherj.api;

import android.util.Log;

import net.bither.bitherj.api.http.BitherUrl;
import net.bither.bitherj.api.http.HttpsGetResponse;
import net.bither.bitherj.utils.Utils;

/**
 * Created by zhangbo on 16/1/9.
 */
public class BlockChainMytransactionsApi extends HttpsGetResponse<String> {

    @Override
    public void setResult(String response) throws Exception {
        this.result = response;
        //Log.i("test34", "BlockChainMytransactionsApi result = " + result);
    }

    public BlockChainMytransactionsApi(String address) {
        String url = Utils.format(BitherUrl.BITHER_BC_GET_BY_ADDRESS, address);
        setUrl(url);
    }

    public BlockChainMytransactionsApi(String address, int offset, int limit) {
        String url = Utils.format(BitherUrl.BITHER_BC_GET_BY_ADDRESS, address);
        setUrl(url + "?offset=" + offset + "&limit=" + limit);
    }

    public BlockChainMytransactionsApi() {
        setUrl(BitherUrl.BITHER_BC_LATEST_BLOCK);
    }

    public BlockChainMytransactionsApi(int txIndex) {
        String url = String.format(BitherUrl.BITHER_BC_TX_INDEX, txIndex);
        setUrl(url);
    }
}
