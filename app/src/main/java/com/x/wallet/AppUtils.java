package com.x.wallet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.x.wallet.lib.eth.api.EtherscanAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    public static final String ACCOUNT_TYPE = "account_type";
    public static final String TX_LIST_SYNCED = "tx_list_synced:";

    public static final String APP_TAG = "XWallet";

    public static final int CREATE_ADDRESS_FAILED_OTHER = -1;
    public static final int CREATE_ADDRESS_OK = 0;
    public static final int CREATE_ADDRESS_FAILED_ACCOUNTNAME_SAME = 1;
    public static final int CREATE_ADDRESS_FAILED_ADDRESS_EXIST = 2;

    public static final int HAS_TOKEN = 1;

    public static final String LOCAL_TOKEN_URL = "file:///android_asset/";
    public static final String TOKEN_GITHUT_URL = "https://raw.githubusercontent.com/TrustWallet/tokens/master/images/";

    public static String getTokenUrl(Context context, String address){
        String name = address + ".png";
        String url;
        if (isAssetsExists(context, name)){
            url = LOCAL_TOKEN_URL + name;
        }else {
            url = new File(context.getCacheDir(),name).getPath();
        }
        return url;
    }

    public static boolean isAssetsExists(Context context, String filename) {
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
    //1. assets
    //2. cache
    //3. github
    //4. EtherScan
    public static void setImage(final ImageView view, final String address){
        final String url = getTokenUrl(view.getContext(), address);
        final File file = new File(url);
        final String iconName = address + ".png";
        if (url.contains("asset")){
            try {
                InputStream in = view.getContext().getAssets().open(iconName);
                if (in.available() != 0){
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    view.setImageBitmap(bitmap);
                }
                in.close();
            }catch (IOException e){

            }
        }else if (file.exists()){
            view.setImageURI(Uri.fromFile(file));
        }else {
            Picasso.get().load(TOKEN_GITHUT_URL + iconName).into(view, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    Log.i(APP_TAG, "set github icon ok!");
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            download(TOKEN_GITHUT_URL + iconName, address);//cache it
                        }
                    });
                }

                @Override
                public void onError(Exception ex) {
                    try {
                        getTokenImage(view, address);
                    } catch (IOException e) {
                        Log.i(APP_TAG, "get token image failure for :" + e.getMessage());
                    }
                }
            });

        }

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
                            download(baseUrl + path, address);//cache it
                        }
                    });
                }
            }
        });

    }

    private static void download(String url, final String address) {
        if (isTokenIconExist(address)){
            return;
        }
        Log.i("@@@@","start download icon in " + url + ";for " + address );
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                saveTokenIcon(bitmap, address);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        //Picasso下载
        Picasso.get().load(url).into(target);

    }
    public static boolean isTokenIconExist(String address){
        final String file = XWalletApplication.getApplication().getCacheDir() + "/" + address + ".png";
        if (new File(file).exists() || isAssetsExists(XWalletApplication.getApplication(), address + ".png")){
            return true;
        }
        return false;
    }

    private static void saveTokenIcon(Bitmap bitmap, String address){
        String imageName = address + ".png";

        File dcimFile = new File(XWalletApplication.getApplication().getCacheDir() + "/" +  imageName);
        if (dcimFile.exists()){
            Log.i("@@@@","already downloaded" + dcimFile.toString());
            return;
        }

        FileOutputStream ostream = null;
        try {
            ostream = new FileOutputStream(dcimFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            Log.i("@@@@"," downloaded icon in " + dcimFile.toString());
            ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeDeletedToken(String address, String tokenName){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        String deletedToken = preferences.getString(address,"");
        if (!deletedToken.contains(tokenName)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(address, deletedToken + " " + tokenName);
            editor.apply();
        }
    }

    public static Boolean hasDeleted(String address, String tokenName){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        String deletedToken = preferences.getString(address,"");
        return deletedToken.contains(tokenName);
    }

    public static void log(String msg){
        Log.i("CoinPay", msg);
    }

    public static String formatDate(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(timeStamp * 1000L);
        return sdf.format(date);
    }

    public static String formatDate(String timeStamp){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(Long.parseLong(timeStamp) * 1000L);
        return sdf.format(date);
    }


    public static Boolean isValideNumber(String str){
        Pattern pattern = Pattern.compile("([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9])");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isFirstTimeToSyncTxList(String address, String syncAddress){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        String syncNames = preferences.getString(TX_LIST_SYNCED + address, "");
        return !syncNames.contains(syncAddress);
    }

    public static void updateSyncAddress(String address, String syncAddress){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(XWalletApplication.getApplication().getApplicationContext());
        String syncNames = preferences.getString(TX_LIST_SYNCED + address, "");
        if (!syncNames.contains(syncAddress)){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(TX_LIST_SYNCED + address, syncNames + " " + syncAddress);
            editor.apply();
        }
    }

    public static View getStubView(Activity activity, int stubId, int viewId) {
        View view = activity.findViewById(viewId);
        if (view == null) {
            ViewStub stub = (ViewStub) activity.findViewById(stubId);
            view = stub.inflate();
        }

        return view;
    }
}
