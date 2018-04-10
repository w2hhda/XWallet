package com.x.wallet.transaction.token;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.x.wallet.AppUtils;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.ui.data.TokenItem;

import java.util.ArrayList;
import java.util.List;

public class ManagerTokenListLoader extends AsyncTaskLoader<List<TokenItem>> {
    private String address;
    private final List<TokenItem> tokenItems = new ArrayList<>();
    public ManagerTokenListLoader(Context context, String address) {
        super(context);
        this.address = address;
    }

    @Override
    public List<TokenItem> loadInBackground() {
        Cursor cursor = null;
        try {
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                    .query(XWalletProvider.CONTENT_URI_TOKEN, null,
                            DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " = ?",
                            new String[]{address}, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    tokenItems.add(TokenItem.createFromCursor(cursor));
                }
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tokenItems;
    }
}
