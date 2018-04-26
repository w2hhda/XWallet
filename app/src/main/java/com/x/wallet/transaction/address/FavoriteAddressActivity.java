package com.x.wallet.transaction.address;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.ui.ActionUtils;
import com.x.wallet.ui.activity.WithBackAppCompatActivity;
import com.x.wallet.ui.adapter.FavoriteAddressAdapter;
import com.x.wallet.ui.data.AddressItem;

public class FavoriteAddressActivity extends WithBackAppCompatActivity {
    private static final int FAVORITE_ADDRESS_LOADER = 894;

    private LoaderManager mLoaderManager;
    private RecyclerView mRecyclerView;
    private FavoriteAddressAdapter mAdapter;
    private TextView mNoFavoriteTv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.favorite_address);
        setContentView(R.layout.favorite_address_activity);
        initView();
    }

    private void initView(){
        mNoFavoriteTv = findViewById(R.id.no_favorite_address);
        initRecyclerView();
        initLoaderManager();
    }

    private void initRecyclerView(){
        mRecyclerView = findViewById(R.id.favorite_address_rv);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mAdapter = new FavoriteAddressAdapter(this, null , 0, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        mAdapter.setItemClickListener(new FavoriteAddressAdapter.ItemClickListener() {
            @Override
            public void onItemClick(AddressItem item) {
                ActionUtils.addFavoriteAddress(FavoriteAddressActivity.this, item);
            }
        });
    }

    private void initLoaderManager(){
        mLoaderManager = getLoaderManager();
        Loader favoriteAddressLoader = mLoaderManager.initLoader(FAVORITE_ADDRESS_LOADER, new Bundle(),
                new FavoriteAddressLoaderCallbacks());
        favoriteAddressLoader.forceLoad();

    }

    private class FavoriteAddressLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        final String order = DbUtils.AddressTableColumns.NAME + " ASC";
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(FavoriteAddressActivity.this, XWalletProvider.CONTENT_URI_ADDRESS, null, null, null, order);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            updateVisibility(data.getCount());
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private void updateVisibility(int count){
        mNoFavoriteTv.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoaderManager != null){
            mLoaderManager.destroyLoader(FAVORITE_ADDRESS_LOADER);
            mLoaderManager = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.manage_favorite_address_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_favorite_address:
                ActionUtils.addFavoriteAddress(this, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
