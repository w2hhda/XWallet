package com.x.wallet.btc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.x.wallet.XWalletApplication;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.balance.RetrofitBtcService;
import com.x.wallet.transaction.balance.RetrofitClient;
import com.x.wallet.transaction.token.TokenUtils;

import org.json.JSONObject;

import java.math.BigDecimal;

import okhttp3.ResponseBody;

/**
 * Created by wuliang on 18-4-13.
 */

public class BtcUtils {
    private static final String TAG = "BtcUtils";
    public static final int BTC_DECIMALS_COUNT = 8;
    public static final Uri BTC_CONTENT_URI = Uri.parse("content://com.x.wallet/btc/tx");

    public static final String BLOCKCHAIN_SERVICE_ACTION = "blockchain_service_action";
    public static final int BLOCKCHAIN_SERVICE_ACTION_START = 0;
    public static final int BLOCKCHAIN_SERVICE_ACTION_STOP_PEER = 1;
    public static final int BLOCKCHAIN_SERVICE_ACTION_START_PEER = 2;

    public static final String CURRENT_BTC_PRICE_KEY = "current_btc_price";
    public static String mCurrentBtcPrice;

    public static void init(){
        AndroidDbImpl androidDb = new AndroidDbImpl();
        androidDb.construct();
        AndroidImplAbstractApp appAndroid = new AndroidImplAbstractApp();
        appAndroid.construct();
        initBtcPrice();
    }

    public static void visitBlockchainService(int action){
        Intent intent = new Intent(XWalletApplication.getApplication().getApplicationContext(), BlockchainService.class);
        intent.putExtra(BLOCKCHAIN_SERVICE_ACTION, action);
        XWalletApplication.getApplication().getApplicationContext().startService(intent);
    }

    public static void stopPeer(int coinType){
        if(coinType == LibUtils.COINTYPE.COIN_BTC){
            BtcUtils.visitBlockchainService(BtcUtils.BLOCKCHAIN_SERVICE_ACTION_STOP_PEER);
        }
    }

    public static void startPeer(int coinType){
        if(coinType == LibUtils.COINTYPE.COIN_BTC){
            BtcUtils.visitBlockchainService(BtcUtils.BLOCKCHAIN_SERVICE_ACTION_START_PEER);
        }
    }

    private static void initBtcPrice(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        mCurrentBtcPrice = preferences.getString(CURRENT_BTC_PRICE_KEY, "0");
    }

    public static void updateCurrentBtcPrice(String currentPrice) {
        if(!mCurrentBtcPrice.equals(currentPrice)){
            mCurrentBtcPrice = currentPrice;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(CURRENT_BTC_PRICE_KEY, currentPrice);
            editor.apply();
        }
    }

    public static void requestCurrencyBtcPrice(String chooseCurrency){
        try{
            RetrofitBtcService getCurrencyToUsdService = RetrofitClient.getBtcService();
            retrofit2.Call<ResponseBody> call = getCurrencyToUsdService.getBtcPriceToCall();
            retrofit2.Response<ResponseBody>  response = call.execute();
            if(response == null){
                return;
            }
            ResponseBody body = response.body();
            if(body == null) return ;
            String result = body.string();
            handleBtcPriceJson(new JSONObject(result), chooseCurrency);
        } catch (Exception e){
            Log.e(TAG, "BtcUtils requestCurrencyBtcPrice exception" , e);
        }
    }

    public static void handleBtcPriceJson(JSONObject jsonObject, String currency){
        try {
            if(jsonObject.has(currency)){
                JSONObject priceObject = jsonObject.getJSONObject(currency);
                if(priceObject.has("last")){
                    BtcUtils.updateCurrentBtcPrice(priceObject.getString("last"));
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "BtcUtils handleBtcPriceJson exception", e);
        }
    }

    public static String getBalanceConversionText(String balance) {
        if (TextUtils.isEmpty(balance) || balance.equals(TokenUtils.ZERO)) return TokenUtils.ZERO;
        BigDecimal bigDecimal = new BigDecimal(balance);
        return TokenUtils.formatConversion(bigDecimal.multiply(new BigDecimal(mCurrentBtcPrice)));
    }
}
