package com.x.wallet.ui.activity;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.transaction.token.InsertTokenAsycTask;
import com.x.wallet.transaction.token.TokenListLoader;
import com.x.wallet.ui.adapter.AllSupportTokenListAdapter;
import com.x.wallet.ui.data.TokenItemBean;

import java.util.List;

/**
 * Created by wuliang on 18-3-30.
 */

public class AddTokenActivity extends WithBackAppCompatActivity {

    private RecyclerView mRecyclerView;
    private View mAddBtn;
    private View mEmptyView;

    private LoaderManager mLoaderManager;
    private static final int TOKEN_LIST_LOADER = 1;
    private AllSupportTokenListAdapter mAdapter;
    private String mAccountAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_token_activity);
        mAccountAddress = getIntent().getStringExtra(AppUtils.ACCOUNT_ADDRESS);

        initRecyclerView();

        initLoaderManager();

        final long accountId = getIntent().getLongExtra(AppUtils.ACCOUNT_ID, -1);
        initAddBtn(accountId, mAccountAddress);
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
        mAdapter = new AllSupportTokenListAdapter(R.layout.token_list_item);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mEmptyView = findViewById(R.id.empty_view);
    }

    private void initLoaderManager(){
        mLoaderManager = getLoaderManager();
        Loader tokenListLoader = getLoaderManager().initLoader(
                TOKEN_LIST_LOADER,
                null,
                mLoaderCallbacks);
        tokenListLoader.forceLoad();
    }

    private void initAddBtn(final long accountId, final String accountAddress){
        mAddBtn = findViewById(R.id.finish_btn);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(accountId >= 0){
                    if(mAdapter.getSelectedTokenItem() != null){
                        new InsertTokenAsycTask(AddTokenActivity.this, accountId, accountAddress,
                                mAdapter.getSelectedTokenItem(),
                                new InsertTokenAsycTask.OnInsertTokenFinishedListener() {
                                    @Override
                                    public void onInsertFinished() {
                                        AddTokenActivity.this.finish();
                                    }
                                }).execute();
                    } else {
                        AddTokenActivity.this.finish();
                    }
                }
            }
        });
        mAdapter.setItemClickListener(new AllSupportTokenListAdapter.ItemClickListener() {
            @Override
            public void onItemClick() {
                mAddBtn.setEnabled(true);
            }
        });
    }

    private final LoaderManager.LoaderCallbacks<List<TokenItemBean>> mLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<TokenItemBean>>() {

                @Override
                public Loader<List<TokenItemBean>> onCreateLoader(int id, Bundle args) {
                    return new TokenListLoader(AddTokenActivity.this, mAccountAddress);
                }

                @Override
                public void onLoadFinished(Loader<List<TokenItemBean>> loader, List<TokenItemBean> tokenItems) {
                    mAdapter.addAll(tokenItems);
                    if(tokenItems == null || tokenItems.size() <= 0){
                        mAddBtn.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onLoaderReset(Loader<List<TokenItemBean>> loader) {

                }
            };
}
