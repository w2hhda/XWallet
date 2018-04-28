package net.bither.bitherj.api.http;

import android.util.Log;

import com.x.wallet.lib.common.LibUtils;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class HttpsPostResponse<T> extends BaseHttpsResponse<T> {

    public void handleHttpPost() throws Exception {
        Response response = null;
        try {
            OkHttpClient okHttpClient = createOkHttpClient();
            MediaType mediaType = MediaType.parse("text/html");
            RequestBody requestBody = RequestBody.create(mediaType, getRequestBody());
            Request request = new Request.Builder()
                    .url(getUrl())
                    .post(requestBody)
                    .build();
            response = okHttpClient.newCall(request).execute();
            int responseCode = response.code();
            Log.i(LibUtils.TAG_BTC, "HttpsGetResponse handleHttpPost responseCode = " + responseCode);
            if (responseCode != 200) {
                return;
            }
            ResponseBody body = response.body();
            if (body != null) {
                String result = body.string();
                setResult(result);
            } else {
                Log.w(LibUtils.TAG_BTC, "HttpsGetResponse handleHttpPost body is null!");
            }
        } catch (Exception e) {
            Log.e(LibUtils.TAG_BTC, "HttpsGetResponse handleHttpPost Exception", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private String getRequestBody() {
        StringBuffer params = new StringBuffer();
        try {
            for (Iterator iter = getParams().entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry element = (Map.Entry) iter.next();
                params.append(element.getKey().toString());
                params.append("=");
                params.append(URLEncoder.encode(element.getValue().toString(),
                        HttpSetting.REQUEST_ENCODING));
                params.append("&");
            }

            if (params.length() > 0) {
                params = params.deleteCharAt(params.length() - 1);
            }
        } catch (Exception e) {
            Log.e(LibUtils.TAG_BTC, "HttpsGetResponse getRequestBody Exception", e);
        }
        return params.toString();
    }

    public abstract Map<String, String> getParams() throws Exception;
}
