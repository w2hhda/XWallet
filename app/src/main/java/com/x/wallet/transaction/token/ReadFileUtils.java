package com.x.wallet.transaction.token;

import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by wuliang on 18-3-30.
 */

public class ReadFileUtils {
    public static String readIntoStr(int id) {
        InputStream stream = XWalletApplication.getApplication().getApplicationContext().getResources().openRawResource(id);
        return read(stream);
    }

    private static String read(InputStream stream) {
        return read(stream, "utf-8");
    }

    private static String read(InputStream inputStream, String encode) {
        BufferedReader reader = null;
        try {
            if (inputStream != null) {
                try {
                    reader = new BufferedReader(new InputStreamReader(inputStream, encode));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    return sb.toString();
                } catch (Exception e) {
                    Log.e(AppUtils.APP_TAG, "ReadFileUtils read", e);
                }
            }
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                Log.e(AppUtils.APP_TAG, "ReadFileUtils read 2", e);
            }
        }

        return null;
    }

}
