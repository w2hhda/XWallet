package com.x.wallet.transaction.token;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.x.wallet.R;
import com.x.wallet.ui.data.TokenItem;
import com.x.wallet.ui.data.TokenItemBean;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by wuliang on 18-3-30.
 */

public class TokenListLoader extends AsyncTaskLoader<List<TokenItemBean>> {

    public TokenListLoader(Context context) {
        super(context);
    }

    @Override
    public List<TokenItemBean> loadInBackground() {
        String allTokenStr = ReadFileUtils.readIntoStr(R.raw.token);
        Type listType = new TypeToken<List<TokenItemBean>>() {}.getType();
        return new Gson().fromJson(allTokenStr, listType);
    }
}
