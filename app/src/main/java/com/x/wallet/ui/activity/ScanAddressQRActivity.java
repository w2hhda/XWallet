package com.x.wallet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.x.wallet.R;
import com.x.wallet.lib.eth.util.qr.AddressEncoder;

import java.io.IOException;
import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Nick on 26/3/2018.
 */

public class ScanAddressQRActivity extends WithBackAppCompatActivity implements ZXingScannerView.ResultHandler{

    public static final int REQUEST_CODE = 100;
    public static final String EXTRA_ADDRESS = "extra_address";
    private ZXingScannerView mScannerView;
    private FrameLayout scanLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_address_ar_activity);
        initView();
    }

    private void initView(){
        this.setTitle("scan address");
        scanLayout = findViewById(R.id.scan_frame_layout);

        mScannerView = new ZXingScannerView(this);
        scanLayout.addView(mScannerView);
        mScannerView.setResultHandler(this);
        ArrayList<BarcodeFormat> supported = new ArrayList<BarcodeFormat>();
        supported.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(supported);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        if (result == null) return;
        String address = result.getText();
        try {
            AddressEncoder scanned = AddressEncoder.decode(address);
            Intent data = new Intent();
            data.putExtra(EXTRA_ADDRESS, scanned.getAddress().toLowerCase());

            setResult(RESULT_OK, data);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScannerView != null){
            mScannerView.stopCamera();
        }
    }
}
