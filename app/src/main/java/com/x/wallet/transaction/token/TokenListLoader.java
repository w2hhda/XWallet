package com.x.wallet.transaction.token;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.x.wallet.R;
import com.x.wallet.db.DbUtils;
import com.x.wallet.ui.data.TokenItemBean;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wuliang on 18-3-30.
 */

public class TokenListLoader extends AsyncTaskLoader<List<TokenItemBean>> {
    private String mAccountAddress;

    public TokenListLoader(Context context, String accountAddress) {
        super(context);
        mAccountAddress = accountAddress;
    }

    @Override
    public List<TokenItemBean> loadInBackground() {
        String allTokenStr = ReadFileUtils.readIntoStr(R.raw.token);
        Type listType = new TypeToken<List<TokenItemBean>>() {}.getType();

        List<TokenItemBean> list = new Gson().fromJson(allTokenStr, listType);
        removeExistToken(list);
        return list;
    }

    public void removeExistToken(List list){
        HashSet<String> result = isExistToken();
        if(result.size() > 0){
            Iterator<TokenItemBean> iterator = list.iterator();
            while(iterator.hasNext()){
                TokenItemBean tokenItemBean = iterator.next();
                if(result.contains(tokenItemBean.getName())){
                    iterator.remove();
                }
            }
        }
    }

    private HashSet<String> isExistToken(){
        HashSet<String> result = new HashSet<>();
        Cursor cursor = null;
        try {
            cursor = DbUtils.queryAccountToken(mAccountAddress);
            if(cursor != null){
                while (cursor.moveToNext()){
                    String tokenName = cursor.getString(0);
                    if (tokenName == null){
                        continue;
                    }
                    result.add(tokenName);
                }
            }
            return result;
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
    }
}
