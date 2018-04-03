package com.x.wallet.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import com.x.wallet.R;

public class ServicePolicyActivity extends WithBackAppCompatActivity {
    public final static String TYPE_TAG = "service_type";
    public final static String TYPE_SERVICE = "service_policy";
    public final static String TYPE_PRIVACY = "privacy_policy";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_policy_activity);
        WebView webView = findViewById(R.id.service_webview);
        if (getIntent().hasExtra(TYPE_TAG)){
            if (getIntent().getStringExtra(TYPE_TAG).equals(TYPE_SERVICE)){
                setTitle(getResources().getString(R.string.service_policy));
                webView.loadUrl("file:///android_asset/xwallet_user_service_policy");
            }
            if (getIntent().getStringExtra(TYPE_TAG).equals(TYPE_PRIVACY)){
                setTitle(getResources().getString(R.string.privacy_policy));
                webView.loadUrl("file:///android_asset/xwallet_private_policy");
            }
        }
    }
}
