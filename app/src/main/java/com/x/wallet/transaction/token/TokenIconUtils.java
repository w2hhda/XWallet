package com.x.wallet.transaction.token;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.XWalletApplication;
import com.x.wallet.lib.eth.api.EtherscanAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by wuliang on 18-4-25.
 */

public class TokenIconUtils {
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
                Log.e(AppUtils.APP_TAG, "TokenIconUtils setImage error", e);
            }
        }else if (file.exists()){
            view.setImageURI(Uri.fromFile(file));
        }else {
            Picasso.get().load(TOKEN_GITHUT_URL + iconName).placeholder(R.drawable.eos_28).into(view, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
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
                        Log.e(AppUtils.APP_TAG, "TokenIconUtils get token image failure for :", e);
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
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        view.setImageResource(R.drawable.eos_28);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Pattern pattern = Pattern.compile("token.images.(.*.png)");
                Matcher matcher = pattern.matcher(result);
                String path = "";
                if(matcher.find()) {
                    final String image = matcher.group();
                    path = image.substring(image.lastIndexOf("/") + 1, image.length());
                    //WeakReference<ImageView> reference = new WeakReference<>(view);
                }

                final String tokenIconUrl = baseUrl + path;
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        if (tokenIconUrl.contains("png")){
                            download(tokenIconUrl, address);//cache it
                            Picasso.get().load(tokenIconUrl).error(R.drawable.eos_28).into(view);
                        }
                    }
                });

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
}
