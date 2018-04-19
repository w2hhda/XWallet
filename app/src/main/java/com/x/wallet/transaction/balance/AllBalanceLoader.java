package com.x.wallet.transaction.balance;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.x.wallet.XWalletApplication;
import com.x.wallet.btc.BtcUtils;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.LibUtils;
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
        BigDecimal[] result = calculateMainCoin(XWalletProvider.CONTENT_URI, new String[]{DbUtils.DbColumns.BALANCE, DbUtils.DbColumns.COIN_TYPE});
        BigDecimal ethBalance = result[0];
        BigDecimal btcBalance = result[1];
        BigDecimal tokenBalance = calculateToken(XWalletProvider.CONTENT_URI_TOKEN, new String[]{DbUtils.TokenTableColumns.BALANCE,
                DbUtils.TokenTableColumns.DECIMALS, DbUtils.TokenTableColumns.RATE});
        BigDecimal lastResult = BigDecimal.ZERO;
        if(btcBalance.compareTo(BigDecimal.ZERO) == 1){
            lastResult = TokenUtils.translate(btcBalance, BtcUtils.BTC_DECIMALS_COUNT).multiply(new BigDecimal(BtcUtils.mCurrentBtcPrice));
        }
        if((ethBalance.compareTo(BigDecimal.ZERO) == 1 || tokenBalance.compareTo(BigDecimal.ZERO) == 1)
                && UsdToCnyHelper.mUsdToCny > 0){
            ethBalance = TokenUtils.calculate(ethBalance, TokenUtils.ETH_DECIMALS);

            tokenBalance = TokenUtils.calculateTokenBalance(tokenBalance);
            lastResult = lastResult.add(ethBalance).add(tokenBalance);
        }

        return lastResult.compareTo(BigDecimal.ZERO) == 1 ? TokenUtils.formatConversion(lastResult) : "0";
    }

    private static BigDecimal[] calculateMainCoin(Uri uri, String[] projection){
        BigDecimal[] result = {BigDecimal.ZERO, BigDecimal.ZERO};
        Cursor cursor = null;
        try{
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                    .query(uri, projection, null, null, null);
            String itemBalance = null;
            int coinType;
            if(cursor != null){
                while (cursor.moveToNext()){
                    itemBalance = cursor.getString(0);
                    if(TextUtils.isEmpty(itemBalance) || itemBalance.equals("0")){
                        continue;
                    }
                    coinType = cursor.getInt(1);
                    switch (coinType){
                        case LibUtils.COINTYPE.COIN_ETH:
                            result[0] = result[0].add(new BigDecimal(itemBalance));
                            break;
                        case LibUtils.COINTYPE.COIN_BTC:
                            result[1] = result[1].add(new BigDecimal(itemBalance));
                            break;
                    }
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
