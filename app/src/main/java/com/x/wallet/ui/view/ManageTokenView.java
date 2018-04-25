package com.x.wallet.ui.view;

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
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.transaction.token.DeleteTokenAsyncTask;
import com.x.wallet.ui.adapter.AccountTokenListAdapter;
import com.x.wallet.ui.data.TokenItem;
import com.x.wallet.ui.dialog.ContentShowDialogHelper;

/**
 * Created by wuliang on 18-4-20.
 */

public class ManageTokenView extends LinearLayout{
    private View mAddTokenView;
    private RecyclerView mRecyclerView;
    private AccountTokenListAdapter mAdapter;

    private long mAccountId;
    private String mAccountAddress;
    private LoaderManager mLoaderManager;
    public static final int TOKEN_LIST_LOADER = 1;

    public ManageTokenView(Context context) {
        super(context);
    }

    public ManageTokenView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ManageTokenView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(LoaderManager loaderManager, long accountId, String accountAddress){
        mLoaderManager = loaderManager;
        mAccountId = accountId;
        mAccountAddress = accountAddress;

        mLoaderManager.initLoader(
                TOKEN_LIST_LOADER,
                new Bundle(),
                new TokenListLoaderCallbacks());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initTokenRelatedViews();
    }


    private void initTokenRelatedViews(){
        mAddTokenView = findViewById(R.id.add_token_tv);
        mAddTokenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.x.wallet.action.ADD_TOKEN_ACTION");
                intent.putExtra(AppUtils.ACCOUNT_ID, mAccountId);
                intent.putExtra(AppUtils.ACCOUNT_ADDRESS, mAccountAddress);
                ManageTokenView.this.getContext().startActivity(intent);
            }
        });

        initRecyclerView();

    }

    private void initRecyclerView(){
        mRecyclerView = findViewById(R.id.recyclerView_manage_token);
        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new AccountTokenListAdapter(this.getContext(), null,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AccountTokenListItem listItem = (AccountTokenListItem) view.getParent();
                        final TokenItem item = listItem.getTokenItem();
                        ContentShowDialogHelper.showConfirmDialog(ManageTokenView.this.getContext(), R.string.delete_token
                                , getResources().getString(R.string.confirm_delete_token, item.getName())
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        new DeleteTokenAsyncTask(ManageTokenView.this.getContext(),
                                                item,
                                                mAccountAddress).execute();
                                    }
                                });
                    }
                });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private class TokenListLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            return new CursorLoader(ManageTokenView.this.getContext(), XWalletProvider.CONTENT_URI_TOKEN, null, DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " = ?", new String[]{mAccountAddress}, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
