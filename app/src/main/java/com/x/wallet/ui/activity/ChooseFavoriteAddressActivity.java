package com.x.wallet.ui.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.ui.adapter.FavoriteAddressAdapter;
import com.x.wallet.ui.data.AddressItem;

public class ChooseFavoriteAddressActivity extends WithBackAppCompatActivity {
    private static final int FAVORITE_ADDRESS_LOADER = 1;

    private LoaderManager mLoaderManager;
    private RecyclerView mRecyclerView;
    private FavoriteAddressAdapter mAdapter;
    private TextView mNoFavoriteTv;

    private String mAddressType;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_address_activity);
        initData();
        initView();
    }

    private void initData(){
        final int typeId = getIntent().getIntExtra(AppUtils.COIN_TYPE, -1);
        switch (typeId){
            case LibUtils.COINTYPE.COIN_BTC:
                mAddressType = AppUtils.COIN_ARRAY[0];
                break;
            case LibUtils.COINTYPE.COIN_ETH:
                mAddressType = AppUtils.COIN_ARRAY[1];
                break;
            default:
                mAddressType = null;
                break;
        }

    }

    private void initView(){
        mNoFavoriteTv = findViewById(R.id.no_favorite_address);
        initRecyclerView();
        initLoaderManager();
    }

    private void initRecyclerView(){
        mRecyclerView = findViewById(R.id.favorite_address_rv);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mAdapter = new FavoriteAddressAdapter(this, null , 0);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        mAdapter.setItemClickListener(new FavoriteAddressAdapter.ItemClickListener() {
            @Override
            public void onItemClick(AddressItem item) {
                Intent intent = new Intent();
                intent.putExtra(AppUtils.EXTRA_ADDRESS, item.getAddress());
                setResult(RESULT_OK, intent);
                ChooseFavoriteAddressActivity.this.finish();
            }
        });
    }

    private void initLoaderManager(){
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(FAVORITE_ADDRESS_LOADER, new Bundle(),
                new FavoriteAddressLoaderCallbacks());
    }

    private void updateVisibility(int count){
        mNoFavoriteTv.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoaderManager != null){
            mLoaderManager.destroyLoader(FAVORITE_ADDRESS_LOADER);
            mLoaderManager = null;
        }
    }

    private class FavoriteAddressLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        final String order = DbUtils.AddressTableColumns.NAME + " ASC";
        final String selection = DbUtils.AddressTableColumns.ADDRESS_TYPE + " = ?";
        final String[] selectionArgs = new String[]{mAddressType};
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(ChooseFavoriteAddressActivity.this, XWalletProvider.CONTENT_URI_ADDRESS, null, selection, selectionArgs, order);
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
}
