package com.x.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.x.wallet.lib.eth.api.EtherscanAPI;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by wuliang on 18-3-13.
 */

public class AppUtils {
    public static final int ACCOUNT_ACTION_TYPE_NEW = 0;
    public static final int ACCOUNT_ACTION_TYPE_IMPORT = 1;

    public static final String[] COIN_ARRAY = {"BTC", "ETH"};

    public static int getMnemonicType(String mnemonicTypeText) {
        return 0;
    }

    public static int getColor(Context context, int colorId) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return context.getResources().getColor(colorId);
        } else{
            return context.getResources().getColor(colorId, null);
        }
    }

    public interface IMPORTTYPE{
        int IMPORT_TYPE_MNEMONIC = 0;
        int IMPORT_TYPE_KEY = 1;
        int IMPORT_TYPE_KEYSTORE = 2;
    }

    public static final String ACTION_TYPE = "action_type";
    public static final String COIN_TYPE  = "coin_type";
    public static final String ACCOUNT_DATA = "account_data";
    public static final String ADDRESS_URI = "address_uri";
    public static final String TRANSACTION_ITEM = "transaction_item";
    public static final String ACCOUNT_ID ="account_id";
    public static final String ACCOUNT_ADDRESS = "account_address";
    public static final String HAS_TOKEN_KEY ="has_token";
    public static final String TOKEN_DATA ="token_data";

    public static final String APP_TAG = "XWallet";

    public static final int CREATE_ADDRESS_FAILED_OTHER = -1;
    public static final int CREATE_ADDRESS_OK = 0;
    public static final int CREATE_ADDRESS_FAILED_ACCOUNTNAME_SAME = 1;
    public static final int CREATE_ADDRESS_FAILED_ADDRESS_EXIST = 2;

    public static final int HAS_TOKEN = 1;

    public static final String LOCAL_TOKEN_URL = "file:///android_asset/";
    public static final String TOKEN_URL = "https://raw.githubusercontent.com/TrustWallet/tokens/master/images/";

    public static String getTokenUrl(Context context, String address){
        String name = address + ".png";
        String url;
        if (isFileExists(context, name)){
            url = LOCAL_TOKEN_URL + name;
        }else {
            url = TOKEN_URL + address.toLowerCase() + ".png";
        }
        return url;
    }

    public static boolean isFileExists(Context context, String filename) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] names = assetManager.list("");

            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(filename.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static void setImage(final ImageView view, final String address){
        final String url = getTokenUrl(view.getContext(), address);

        Picasso.get().load(url).into(view, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                Log.i(AppUtils.APP_TAG,"setImage Ok!");
            }

            @Override
            public void onError(Exception e) {
                try {
                    getTokenImage(view, address);
                }catch (IOException io){
                    Log.e(AppUtils.APP_TAG, "setImage failure for Token:" + address);
                }
            }
        });
    }

    public static void getTokenImage(final ImageView view, final String address) throws IOException{
        final String url = "https://etherscan.io/token/" + address;
        final String baseUrl = "https://etherscan.io/token/images/";
        EtherscanAPI.getInstance().get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Pattern pattern = Pattern.compile("token.*/(.*.png)");
                Matcher matcher = pattern.matcher(result);
                if(matcher.find()) {

                    final String image = matcher.group();
                    final String path = image.substring(image.lastIndexOf("/") + 1, image.length());
                    //WeakReference<ImageView> reference = new WeakReference<>(view);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.get().load(baseUrl + path).error(R.drawable.eos_28).into(view);

                        }
                    });
                }
            }
        });

    }

    public static void writeDeletedToken(String address, String tokenName){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        String deletedToken = preferences.getString(address,"");
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(address, deletedToken + " " + tokenName);
        editor.apply();
    }

    public static Boolean hasDeleted(String address, String tokenName){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        String deletedToken = preferences.getString(address,"");
        return deletedToken.contains(tokenName);
    }
}
