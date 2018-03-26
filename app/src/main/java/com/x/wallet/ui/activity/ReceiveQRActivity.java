package com.x.wallet.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.x.wallet.R;
import com.x.wallet.lib.eth.util.qr.AddressEncoder;
import com.x.wallet.lib.eth.util.qr.Contents;
import com.x.wallet.lib.eth.util.qr.QREncoder;

/**
 * Created by Nick on 26/3/2018.
 */

public class ReceiveQRActivity extends WithBackAppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_qr_activity);

        String address = "";
        Intent intent = getIntent();
        if (intent != null){
            address = intent.getStringExtra(AccountDetailActivity.SHARE_ADDRESS_EXTRA);
        }
        if (address != "") {
            initView(address);
        }else {
            Toast.makeText(this, "error address!", Toast.LENGTH_SHORT);
        }
    }

    private void initView(final String address){
        this.setTitle(getResources().getString(R.string.receipt_transaction));

        ImageView imageView = findViewById(R.id.address_qr_img);
        TextView addressTv = findViewById(R.id.receive_address_tv);
        Button copyBtn = findViewById(R.id.copy_address_btn);
        addressTv.setText(address);

        final float scale = getResources().getDisplayMetrics().density;
        int qrCodeDimention = (int) (310 * scale + 0.5f);

        QREncoder qrCodeEncoder = new QREncoder( AddressEncoder.encodeERC(new AddressEncoder(address)) , null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", address);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ReceiveQRActivity.this, R.string.has_copied_address, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
