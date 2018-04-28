package com.x.wallet.ui.activity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.transaction.FavoriteAddressDbAsycTask;
import com.x.wallet.ui.ActionUtils;
import com.x.wallet.ui.adapter.FavoriteAddressAdapter;
import com.x.wallet.ui.data.AddressItem;
import com.x.wallet.ui.helper.FavoriteAddressHelper;

public class FavoriteAddressActivity extends WithBackAppCompatActivity {
    private static final int FAVORITE_ADDRESS_LOADER = 1;
    private static final int SHARE  = 0;
    private static final int DELETE = 1;

    private LoaderManager mLoaderManager;
    private RecyclerView mRecyclerView;
    private FavoriteAddressAdapter mAdapter;
    private TextView mNoFavoriteTv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mAdapter = new FavoriteAddressAdapter(this, null , 0);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        mAdapter.setItemClickListener(new FavoriteAddressAdapter.ItemClickListener() {
            @Override
            public void onItemClick(AddressItem item) {
                ActionUtils.editFavoriteAddress(FavoriteAddressActivity.this, item);
            }
        });

        mAdapter.setItemLongClickListener(new FavoriteAddressAdapter.ItemLongClickListener() {
            @Override
            public void onLongClick(AddressItem item) {
                showActions(FavoriteAddressActivity.this, item);
            }
        });
    }

    private void initLoaderManager(){
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(FAVORITE_ADDRESS_LOADER, new Bundle(), new FavoriteAddressLoaderCallbacks());
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
                ActionUtils.editFavoriteAddress(this, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showActions(Context context, final AddressItem item){
        final String[] actions = context.getResources().getStringArray(R.array.support_long_click_action_array);
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(R.string.action_to_favorite_address).setSingleChoiceItems(
                new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice, actions), -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case SHARE:
                                shareAddress(item);
                                break;
                            case DELETE:
                                deleteAddress(item);
                                break;
                        }
                        dialog.dismiss();
                    }
                }
        ).create();
        dialog.show();
    }

    private void shareAddress(AddressItem item){
        Intent textIntent = new Intent(Intent.ACTION_SEND);
        textIntent.setType("text/plain");
        textIntent.putExtra(Intent.EXTRA_TEXT, item.toString());
        startActivity(Intent.createChooser(textIntent, getString(R.string.share)));
    }

    private void deleteAddress(AddressItem item){
        final FavoriteAddressDbAsycTask.OnDataActionFinishedListener listener = new FavoriteAddressDbAsycTask.OnDataActionFinishedListener() {
            @Override
            public void onDataActionFinished(boolean isSuccess) {
                if (isSuccess){
                    FavoriteAddressHelper.alertMsg(FavoriteAddressActivity.this, getString(R.string.favorite_address_delete_success));
                }else {
                    FavoriteAddressHelper.alertMsg(FavoriteAddressActivity.this, getString(R.string.favorite_address_delete_failed));
                }
            }
        };
        FavoriteAddressHelper.deleteFavoriteAddress(this, item, listener);
    }
}
