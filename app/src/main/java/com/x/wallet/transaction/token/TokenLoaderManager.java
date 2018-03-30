package com.x.wallet.transaction.token;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.transaction.balance.ItemLoadedCallback;
import com.x.wallet.transaction.balance.ItemLoadedFuture;
import com.x.wallet.ui.data.TokenItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by wuliang on 18-3-26.
 */

public class TokenLoaderManager extends BackgroundLoaderManager {
    private Context mContext;

    public TokenLoaderManager(Context context) {
        super(context);
        mContext = context;
    }

    public ItemLoadedFuture getTokenList(Uri uri, final ItemLoadedCallback<TokenLoaded> callback) {
        Log.i(AppUtils.APP_TAG, "TokenLoaderManager getTokenList uri = " + uri);
        if (uri == null) {
            return null;
        }

        final boolean taskExists = mPendingTaskUris.contains(uri);
        final boolean callbackRequired = (callback != null);

        if (callbackRequired) {
            addCallback(uri, callback);
        }

        if (!taskExists) {
            mPendingTaskUris.add(uri);
            Log.i(AppUtils.APP_TAG, "TokenLoaderManager getTokenList start task.");
            Runnable task = new TokenListTask(uri);
            mExecutor.execute(task);
        }
        return new ItemLoadedFuture() {
            private boolean mIsDone;

            public void cancel(Uri uri) {
                cancelCallback(callback);
            }

            public void setIsDone(boolean done) {
                mIsDone = done;
            }

            public boolean isDone() {
                return mIsDone;
            }
        };
    }

    public class TokenListTask implements Runnable {
        private final Uri mUri;

        public TokenListTask(Uri uri) {
            mUri = uri;
        }


        public void run() {
            final List<TokenItem> tokenList = new ArrayList<>();
            String accountId = mUri.getLastPathSegment();
            Cursor cursor = null;
            try {
                cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                        .query(XWalletProvider.CONTENT_URI_TOKEN, null,
                                DbUtils.TokenTableColumns.ACCOUNT_ID + " = ?",
                                new String[]{accountId}, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        tokenList.add(TokenItem.createFromCursor(cursor));
                    }
                }
                Log.i(AppUtils.APP_TAG, "size = " + tokenList.size());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            mCallbackHandler.post(new Runnable() {
                public void run() {
                    final Set<ItemLoadedCallback> callbacks = mCallbacks.get(mUri);
                    if (callbacks != null) {
                        // Make a copy so that the callback can unregister itself
                        for (final ItemLoadedCallback<TokenLoaded> callback : asList(callbacks)) {
                            TokenLoaded pduLoaded = new TokenLoaded(tokenList);
                            callback.onItemLoaded(pduLoaded, null);
                        }
                    }
                    mCallbacks.remove(mUri);
                    mPendingTaskUris.remove(mUri);
                }
            });
        }
    }

    public static class TokenLoaded {
        public final List<TokenItem> mTokenList;

        public TokenLoaded(List<TokenItem> tokenList) {
            mTokenList = tokenList;
        }
    }

    @Override
    public String getTag() {
        return AppUtils.APP_TAG;
    }
}
