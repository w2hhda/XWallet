/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.x.wallet.transaction.balance;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

abstract class BackgroundLoaderManager {
    private static final int MAX_THREADS = 2;

    /**
     * URIs for which tasks are currently enqueued. Don't enqueue new tasks for
     * these, just add new callbacks.
     */
    protected final Set<String> mPendingTaskUris;

    protected final HashMap<String, Set<ItemLoadedCallback>> mCallbacks;

    protected final Executor mExecutor;

    protected final Handler mCallbackHandler;

    BackgroundLoaderManager(Context context) {
        mPendingTaskUris = new HashSet<String>();
        mCallbacks = new HashMap<String, Set<ItemLoadedCallback>>();
        final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        final int poolSize = MAX_THREADS;
        mExecutor = new ThreadPoolExecutor(
                poolSize, poolSize, 5, TimeUnit.SECONDS, queue,
                new BackgroundLoaderThreadFactory(getTag()));
        mCallbackHandler = new Handler();
    }

    /**
     * Release memory if possible.
     */
    public void onLowMemory() {
        clear();
    }

    public void clear() {
    }

    /**
     * Return a tag that will be used to name threads so they'll be visible in the debugger.
     */
    public abstract String getTag();

    /**
     * Attempts to add a callback for a resource.
     *
     * @param address the {@link String} of the resource for which a callback is
     *            desired.
     * @param callback the callback to register.
     * @return {@code true} if the callback is guaranteed to be invoked with
     *         a non-null result (as long as there is no error and the
     *         callback is not canceled), or {@code false} if the callback
     *         cannot be registered with this task because the result for
     *         the desired {@link Uri} has already been discarded due to
     *         low-memory.
     * @throws NullPointerException if either argument is {@code null}
     */
    public boolean addCallback(String address, ItemLoadedCallback callback) {
        if (address == null) {
            throw new NullPointerException("uri is null");
        }
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        Set<ItemLoadedCallback> callbacks = mCallbacks.get(address);
        if (callbacks == null) {
            callbacks = new HashSet<ItemLoadedCallback>(4);
            mCallbacks.put(address, callbacks);
        }
        callbacks.add(callback);
        return true;
    }

    public void cancelCallback(ItemLoadedCallback callback) {
        for (final String address : mCallbacks.keySet()) {
            final Set<ItemLoadedCallback> callbacks = mCallbacks.get(address);
            callbacks.remove(callback);
        }
    }

    /**
     * Copies the elements of a {@link Set} into an {@link ArrayList}.
     */
    @SuppressWarnings("unchecked")
    protected static <T> ArrayList<T> asList(Set<T> source) {
        return new ArrayList<T>(source);
    }

    /**
     * {@link ThreadFactory} which sets a meaningful name for the thread.
     */
    private static class BackgroundLoaderThreadFactory implements ThreadFactory {
        private final AtomicInteger mCount = new AtomicInteger(1);
        private final String mTag;

        public BackgroundLoaderThreadFactory(String tag) {
            mTag = tag;
        }

        public Thread newThread(final Runnable r) {
            Thread t =  new Thread(r, mTag + "-" + mCount.getAndIncrement());

            if (t.getPriority() != Thread.MIN_PRIORITY)
                t.setPriority(Thread.MIN_PRIORITY);

            return t;
        }
    }
}
