package com.x.wallet.transaction.balance;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;

import java.math.BigDecimal;

/**
 * Created by wuliang on 18-3-30.
 */

public class AllBalanceLoader extends AsyncTaskLoader<String> {

    public AllBalanceLoader(Context context) {
        super(context);
    }

    @Override
    public String loadInBackground() {
        BigDecimal ethBalance = calculate(XWalletProvider.CONTENT_URI, new String[]{DbUtils.DbColumns.BALANCE});
        BigDecimal tokenBalance = calculate(XWalletProvider.CONTENT_URI_TOKEN, new String[]{DbUtils.TokenTableColumns.BALANCE});
        //Log.i("allbalance", "AllBalanceLoader loadInBackground ethBalance = " + ethBalance);
        //Log.i("allbalance", "AllBalanceLoader loadInBackground tokenBalance = " + tokenBalance);
        //Log.i("allbalance", "AllBalanceLoader loadInBackground BalanceConversionUtils.mEthToUsd = " + BalanceConversionUtils.mEthToUsd);
        //Log.i("allbalance", "AllBalanceLoader loadInBackground UsdToCnyHelper.mUsdToCny = " + UsdToCnyHelper.mUsdToCny);
        if((ethBalance.compareTo(BigDecimal.ZERO) == 1 || tokenBalance.compareTo(BigDecimal.ZERO) == 1)
                && UsdToCnyHelper.mUsdToCny > 0){
            ethBalance = ethBalance.divide(TokenUtils.translateDecimalsIntoBigDecimal(18))
                    .multiply(new BigDecimal(BalanceConversionUtils.mEthToUsd))
                    .multiply(new BigDecimal(UsdToCnyHelper.mUsdToCny));
            tokenBalance = tokenBalance.multiply(new BigDecimal(UsdToCnyHelper.mUsdToCny));
            return TokenUtils.format(ethBalance.add(tokenBalance));
        }
        return "0";
    }

    private static BigDecimal calculate(Uri uri, String[] selection){
        //Log.i("allbalance", "AllBalanceLoader calculate start uri = " + uri);
        BigDecimal result = BigDecimal.ZERO;
        Cursor cursor = null;
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                    .query(uri, selection, null, null, null);
            String itemBalance = null;
            if(cursor != null){
                while (cursor.moveToNext()){
                    itemBalance = cursor.getString(0);
                    //Log.i("allbalance", "AllBalanceLoader calculate itemBalance = " + itemBalance);
                    if(TextUtils.isEmpty(itemBalance) || itemBalance.equals("0")){
                        continue;
                    }
                    result = result.add(new BigDecimal(itemBalance));
                    itemBalance = null;
                }
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }

        //Log.i("allbalance", "AllBalanceLoader calculate end uri = " + uri);
        //Log.i("allbalance", "                          ");
        return result;
    }
}
