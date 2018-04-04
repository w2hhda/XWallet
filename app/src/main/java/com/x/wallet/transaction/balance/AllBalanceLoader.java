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
        BigDecimal ethBalance = calculateEth(XWalletProvider.CONTENT_URI, new String[]{DbUtils.DbColumns.BALANCE});
        BigDecimal tokenBalance = calculateToken(XWalletProvider.CONTENT_URI_TOKEN, new String[]{DbUtils.TokenTableColumns.BALANCE,
                DbUtils.TokenTableColumns.DECIMALS, DbUtils.TokenTableColumns.RATE});
        if((ethBalance.compareTo(BigDecimal.ZERO) == 1 || tokenBalance.compareTo(BigDecimal.ZERO) == 1)
                && UsdToCnyHelper.mUsdToCny > 0){
            ethBalance = TokenUtils.calculate(ethBalance, TokenUtils.ETH_DECIMALS);

            tokenBalance = TokenUtils.calculateTokenBalance(tokenBalance);
            return TokenUtils.formatConversion(ethBalance.add(tokenBalance));
        }
        return "0";
    }

    private static BigDecimal calculateEth(Uri uri, String[] selection){
        BigDecimal result = BigDecimal.ZERO;
        Cursor cursor = null;
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                    .query(uri, selection, null, null, null);
            String itemBalance = null;
            if(cursor != null){
                while (cursor.moveToNext()){
                    itemBalance = cursor.getString(0);
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

        return result;
    }

    private static BigDecimal calculateToken(Uri uri, String[] selection){
        BigDecimal result = BigDecimal.ZERO;
        Cursor cursor = null;
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                    .query(uri, selection, null, null, null);
            String itemBalance = null;
            if(cursor != null){
                while (cursor.moveToNext()){
                    itemBalance = cursor.getString(0);
                    if(TextUtils.isEmpty(itemBalance) || itemBalance.equals("0")){
                        continue;
                    }
                    result = result.add(TokenUtils.calculateTokenBalance(itemBalance, cursor.getInt(1), cursor.getDouble(2)));
                    itemBalance = null;
                }
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }
}
