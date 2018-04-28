package net.bither.bitherj.api.http;

import android.util.Log;

import com.x.wallet.lib.common.LibUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class HttpsGetResponse<T> extends BaseHttpsResponse<T> {

    public void handleHttpGet() throws Exception {
        Response response = null;
        try {
            OkHttpClient okHttpClient = createOkHttpClient();
            if (okHttpClient != null) {
                Request request = new Request.Builder()
                        .url(getUrl())
                        .build();
                response = okHttpClient.newCall(request).execute();
                int responseCode = response.code();
                Log.i(LibUtils.TAG_BTC, "HttpsGetResponse handleHttpGet responseCode = " + responseCode);
                if (responseCode != 200) {
                    return;
                }
                ResponseBody body = response.body();
                if (body != null) {
                    String result = body.string();
                    setResult(result);
                } else {
                    Log.w(LibUtils.TAG_BTC, "HttpsGetResponse handleHttpGet body is null!");
                }
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
