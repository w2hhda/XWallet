package com.x.wallet.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.lib.common.LibUtils;
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
    public static final int REQUEST_CAMERA_PERMISSION = 106;
    private int mCoinType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_address_ar_activity);
        mCoinType = getIntent().getIntExtra(AppUtils.COIN_TYPE, -1);
        initView();
    }

    private void initView(){
        scanLayout = findViewById(R.id.scan_frame_layout);
        if (hasPermission()) {
            initQRScan();
        }else {
            askForPermissionRead();
        }
    }

    private void initQRScan(){
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
            data.putExtra(EXTRA_ADDRESS, mCoinType == LibUtils.COINTYPE.COIN_BTC ? scanned.getAddress() : scanned.getAddress().toLowerCase());

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

    public boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public void askForPermissionRead() {
        if (Build.VERSION.SDK_INT < 23) return;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initQRScan();
                } else {
                    Toast.makeText(this, "Please grant camera permission in order to read QR codes", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
