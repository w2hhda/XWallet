package com.x.wallet.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.ui.ActionUtils;
import com.x.wallet.ui.adapter.ManageAllAccountListAdapter;
import com.x.wallet.ui.data.AccountItem;
import com.x.wallet.ui.view.AccountListItem;
import com.x.wallet.ui.view.ManageAllAccountListItem;

/**
 * Created by wuliang on 18-3-28.
 */

public class ManageAllAccountActivity extends WithBackAppCompatActivity {
    private ManageAllAccountListAdapter mManageAllAccountListAdapter;
    private LoaderManager mLoaderManager;
    private RecyclerView mRecyclerView;

    private static final int ACCOUNT_LIST_LOADER = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_all_account_activity);
        super.setTitle(getResources().getString(R.string.manage_account));
        initRecyclerView();
        init();
        XWalletApplication.getApplication().getBalanceLoaderManager().getBalance(null);
    }

    private void initRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        mManageAllAccountListAdapter = new ManageAllAccountListAdapter(this, null, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ManageAllAccountListItem listItem = (ManageAllAccountListItem) view;
                AccountItem accountItem = listItem.getAccountItem();
                Intent intent = new Intent("com.x.wallet.action.MANAGE_ACCOUNT_ACTION");
                intent.putExtra(AppUtils.ACCOUNT_DATA, AccountItem.translateToSerializable(accountItem));
                startActivity(intent);
            }
        });

        mRecyclerView.setAdapter(mManageAllAccountListAdapter);
    }

    private void init() {
        final Bundle args = new Bundle();
        mLoaderManager = getSupportLoaderManager();
        mLoaderManager.initLoader(ACCOUNT_LIST_LOADER, args, new AccountListLoaderCallbacks());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(ACCOUNT_LIST_LOADER);
            mLoaderManager = null;
        }
    }

    private class AccountListLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            return new CursorLoader(ManageAllAccountActivity.this, XWalletProvider.CONTENT_URI,
                    null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.i(AppUtils.APP_TAG, "ManageAllAccountActivity onLoadFinished cursor.count = " + cursor.getCount());
            mManageAllAccountListAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.add_acount_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_account:
                ActionUtils.handleAddAccountAction(this, AppUtils.ACCOUNT_ACTION_TYPE_NEW);
                return true;
            case R.id.action_import_account:
                ActionUtils.handleAddAccountAction(this, AppUtils.ACCOUNT_ACTION_TYPE_IMPORT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
