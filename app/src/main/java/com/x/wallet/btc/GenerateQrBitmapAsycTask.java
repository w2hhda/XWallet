package com.x.wallet.btc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.lib.eth.util.qr.AddressEncoder;
import com.x.wallet.lib.eth.util.qr.Contents;
import com.x.wallet.lib.eth.util.qr.QREncoder;

/**
 * Created by wuliang on 18-3-15.
 */

public class GenerateQrBitmapAsycTask extends AsyncTask<Void, Void, Bitmap>{
    private Context mContext;
    private String mAddress;
    private int mCoinType;

    private OnQrBitmapGenerateFinishedListener mOnQrBitmapGenerateFinishedListener;

    public GenerateQrBitmapAsycTask(Context context, String address, int coinType,
                                    OnQrBitmapGenerateFinishedListener onQrBitmapGenerateFinishedListener) {
        mContext = context;
        mAddress = address;
        mCoinType = coinType;
        mOnQrBitmapGenerateFinishedListener = onQrBitmapGenerateFinishedListener;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        switch (mCoinType){
            case LibUtils.COINTYPE.COIN_BTC:
                return createBtcQrBitmap();
            case LibUtils.COINTYPE.COIN_ETH:
                return createEthQrBitmap();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(mOnQrBitmapGenerateFinishedListener != null){
            mOnQrBitmapGenerateFinishedListener.onQrBitmapGenerate(bitmap);
        }
    }

    private Bitmap createBtcQrBitmap(){
        return Qr.bitmap(mAddress, mContext.getResources().getDimensionPixelSize(R.dimen.qr_bitmap_wh),
                Color.BLACK, Color.WHITE, mContext.getResources().getDimensionPixelSize(R.dimen.qr_bitmap_marginsize));
    }

    private Bitmap createEthQrBitmap(){
        try {
            final float scale = mContext.getResources().getDisplayMetrics().density;
            int qrCodeDimention = (int) (310 * scale + 0.5f);

            QREncoder qrCodeEncoder = new QREncoder( AddressEncoder.encodeERC(new AddressEncoder(mAddress)) , null,
                    Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);
            return qrCodeEncoder.encodeAsBitmap();
        } catch (Exception e){
            Log.e(AppUtils.APP_TAG, "GenerateQrBitmapAsycTask createEthQrBitmap", e);
        }
        return null;
    }

    public interface OnQrBitmapGenerateFinishedListener{
        void onQrBitmapGenerate(Bitmap bitmap);
    }
}
