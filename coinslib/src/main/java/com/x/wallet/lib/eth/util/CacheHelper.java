package com.x.wallet.lib.eth.util;

import com.x.wallet.lib.eth.cache.MemoryCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CacheHelper {

    public static final String TYPE_TOKEN = "TOKEN_";
    public static final String TYPE_TXS_NORMAL = "TXS_NORMAL_";
    public static final String TYPE_TXS_INTERNAL = "TXS_INTERNAL_";
    public static final String TYPE_BALANCES = "BALANCES_";
    public static final String TYPE_PRICE = "PRICE_";
    public static final String TYPE_EXCHANGE_RATE = "EXCHANGE_RATE_";

    private MemoryCache cache = MemoryCache.instance();
    private List<String> keySet = new ArrayList<>();

    private HashMap<String, String> map = new HashMap<String, String>();
    private static CacheHelper instance;

    public static CacheHelper instance() {
        if (instance == null){
            synchronized (CacheHelper.class){
                if (instance == null){
                    instance = new CacheHelper();
                }
            }
        }
        return instance;
    }

    public void put(String type, String address, String response) {
        String key = type + address;
        keySet.add(key);
        cache.put(key, response);
    }

    public String get(String type, String address) {
        Object result = cache.get(type + address);
        if (result != null){
            return result.toString();
        }
        return null;
    }

    public boolean contains(String type, String address) {
        return keySet.contains(type + address);
    }

    public void evictAll(){
        for (String key: keySet){
            cache.remove(key);
        }
        cache.evictAll();
    }

}
