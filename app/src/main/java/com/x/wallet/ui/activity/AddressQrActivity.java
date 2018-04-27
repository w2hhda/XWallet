package com.x.wallet.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.btc.GenerateQrBitmapAsycTask;

/**
 * Created by Nick on 26/3/2018.
 */

public class AddressQrActivity extends WithBackAppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address_qr_activity);

        String address = "";
        Intent intent = getIntent();
        if (intent != null){
            address = intent.getStringExtra(AccountDetailActivity.SHARE_ADDRESS_EXTRA);
            if (!TextUtils.isEmpty(address)) {
                initView(address, intent.getIntExtra(AppUtils.COIN_TYPE, -1));
            }
        }
    }

    private void initView(final String address, int coinType){
        final ImageView imageView = findViewById(R.id.address_qr_img);
        TextView addressTv = findViewById(R.id.receive_address_tv);
        Button copyBtn = findViewById(R.id.copy_address_btn);
        addressTv.setText(address);

        new GenerateQrBitmapAsycTask(this, address, coinType, new GenerateQrBitmapAsycTask.OnQrBitmapGenerateFinishedListener() {
            @Override
            public void onQrBitmapGenerate(Bitmap bitmap) {
                if(bitmap != null){
                    imageView.setImageBitmap(bitmap);
                }
            }
        }).execute();

        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", address);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(AddressQrActivity.this, R.string.has_copied_address, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
