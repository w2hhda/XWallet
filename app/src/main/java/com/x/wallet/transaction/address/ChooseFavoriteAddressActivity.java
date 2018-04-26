package com.x.wallet.transaction.address;

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
import com.x.wallet.ui.activity.WithBackAppCompatActivity;
import com.x.wallet.ui.adapter.FavoriteAddressAdapter;
import com.x.wallet.ui.data.AddressItem;

public class ChooseFavoriteAddressActivity extends WithBackAppCompatActivity {
    public static final int REQUEST_CODE = 936;
    private static final int FAVORITE_ADDRESS_LOADER = 894;
    public static final String EXTRA_ADDRESS = "extra_address";

    private LoaderManager mLoaderManager;
    private RecyclerView mRecyclerView;
    private FavoriteAddressAdapter mAdapter;
    private TextView mNoFavoriteTv;
    private String coinType;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.select_address);
        setContentView(R.layout.favorite_address_activity);
        getCoinType();
        initView();
    }

    private void getCoinType(){
        final int typeId = getIntent().getIntExtra(AppUtils.COIN_TYPE, -1);
        switch (typeId){
            case 0:
                coinType = AppUtils.COIN_ARRAY[0];
                break;
            case 1:
                coinType = AppUtils.COIN_ARRAY[1];
                break;
            case -1:
            default:
                coinType = null;
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
        mAdapter = new FavoriteAddressAdapter(this, null , 0, true);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        mAdapter.setItemClickListener(new FavoriteAddressAdapter.ItemClickListener() {
            @Override
            public void onItemClick(AddressItem item) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_ADDRESS, item.getAddress());
                setResult(RESULT_OK, intent);
                ChooseFavoriteAddressActivity.this.finish();
            }
        });
    }

    private void initLoaderManager(){
        mLoaderManager = getLoaderManager();
        Loader favoriteAddressLoader = mLoaderManager.initLoader(FAVORITE_ADDRESS_LOADER, new Bundle(),
                new ChooseFavoriteAddressActivity.FavoriteAddressLoaderCallbacks());
        favoriteAddressLoader.forceLoad();

    }

    private void updateVisibility(int count){
        mNoFavoriteTv.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
    }

    private class FavoriteAddressLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        final String order = DbUtils.AddressTableColumns.NAME + " ASC";
        final String selection = DbUtils.AddressTableColumns.ADDRESS_TYPE + " = ?";
        final String[] projection = new String[]{DbUtils.AddressTableColumns.ADDRESS};
        final String[] selectionArgs = new String[]{coinType};
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
