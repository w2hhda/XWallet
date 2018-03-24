package com.x.wallet.lib.eth.cache;

import android.util.LruCache;

/**
 * Created by Nick on 23/3/2018.
 */

public class MemoryCache extends LruCache {
    private static MemoryCache instance;

    private MemoryCache(int size){
        super(size);
    }

    public static MemoryCache instance(){
        if (instance == null){
            synchronized (MemoryCache.class){
                if (instance == null){
                    int maxSize = (int)Runtime.getRuntime().maxMemory();
                    int cacheSize = maxSize / 8;
                    instance = new MemoryCache(cacheSize);
                }
            }
        }
        return instance;
    }
    @Override
    protected int sizeOf(Object key, Object value) {
        if (key != null && value != null){
            return (key.toString().length() + value.toString().length()) / 1024;
        }
        return super.sizeOf(key, value);
    }
}
