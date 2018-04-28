package net.bither.bitherj.api.http;

import android.util.Log;

import com.x.wallet.lib.common.LibUtils;

import java.security.KeyStore;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public abstract class BaseHttpsResponse<T> {
    protected T result;
    private String mUrl;


    public T getResult() {
        return result;
    }

    public abstract void setResult(String response) throws Exception;

    protected String getUrl() {
        return mUrl;
    }

    protected void setUrl(String url) {
        this.mUrl = url;
    }

    public static OkHttpClient createOkHttpClient() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            return createOkHttpClientBuilder()
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .build();
        } catch (Exception e) {
            Log.e(LibUtils.TAG_BTC, "BaseHttpsResponse createOkHttpClient Exception", e);
            return createOkHttpClientBuilder().build();
        }
    }

    public static OkHttpClient.Builder createOkHttpClientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(HttpSetting.HTTP_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(HttpSetting.HTTP_SO_TIMEOUT, TimeUnit.SECONDS);
    }

}
