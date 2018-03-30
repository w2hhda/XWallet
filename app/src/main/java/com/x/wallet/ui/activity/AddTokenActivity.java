package com.x.wallet.ui.activity;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.x.wallet.R;
import com.x.wallet.transaction.token.TokenListLoader;
import com.x.wallet.ui.adapter.RecyclerViewArrayAdapter;
import com.x.wallet.ui.data.TokenItem;

import java.util.List;

/**
 * Created by wuliang on 18-3-30.
 */

public class AddTokenActivity extends WithBackAppCompatActivity {

    private View mAddBtn;

    private RecyclerView mRecyclerView;
    private LoaderManager mLoaderManager;
    private static final int TOKEN_LIST_LOADER = 1;
    private RecyclerViewArrayAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_token_activity);
        initRecyclerView();

        initLoaderManager();

        mAddBtn = findViewById(R.id.finish_btn);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("test", "" + mAdapter.getSelectedTokenItem());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoaderManager != null){
            mLoaderManager.destroyLoader(TOKEN_LIST_LOADER);
            mLoaderManager = null;
        }
    }

    private void initRecyclerView(){
        mRecyclerView = findViewById(R.id.recyclerView);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new RecyclerViewArrayAdapter(R.layout.token_list_item);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void initLoaderManager(){
        mLoaderManager = getLoaderManager();
        Loader tokenListLoader = getLoaderManager().initLoader(
                TOKEN_LIST_LOADER,
                null,
                mLoaderCallbacks);
        tokenListLoader.forceLoad();
    }

    private final LoaderManager.LoaderCallbacks<List<TokenItem>> mLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<TokenItem>>() {

                @Override
                public Loader<List<TokenItem>> onCreateLoader(int id, Bundle args) {
                    return new TokenListLoader(AddTokenActivity.this);
                }

                @Override
                public void onLoadFinished(Loader<List<TokenItem>> loader, List<TokenItem> tokenItems) {
                    mAdapter.addAll(tokenItems);
                }

                @Override
                public void onLoaderReset(Loader<List<TokenItem>> loader) {

                }
            };
}
