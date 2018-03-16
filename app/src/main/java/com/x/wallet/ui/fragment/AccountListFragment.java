package com.x.wallet.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.ui.adapter.AccountListAdapter;
import com.x.wallet.ui.data.AccountItem;
import com.x.wallet.ui.view.AccountListItem;

/**
 * Created by wuliang on 18-3-13.
 */

public class AccountListFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private AccountListAdapter mAccountListAdapter;
    private View mAddAccountView;
    private LoaderManager mLoaderManager;

    private static final int ACCOUNT_LIST_LOADER = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountListAdapter = new AccountListAdapter(getActivity(), null, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AccountListItem listItem = (AccountListItem) view;
                AccountItem accountItem = listItem.getAccountItem();
                Intent intent = new Intent("com.x.wallet.action.SEE_ACCOUNT_DETAIL_ACTION");
                intent.putExtra(AppUtils.ACCOUNT_DATA, accountItem);
                AccountListFragment.this.getActivity().startActivity(intent);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment, container, false);
        initRecyclerView(view);
        initAddAccountView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init(getLoaderManager());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(ACCOUNT_LIST_LOADER);
            mLoaderManager = null;
        }
    }

    private void initRecyclerView(View rootView){
        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAccountListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this.getActivity(),DividerItemDecoration.VERTICAL));
    }

    private void initAddAccountView(View rootView){
        mAddAccountView = rootView.findViewById(R.id.add_account_btn);
        mAddAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountListFragment.this.getActivity());
                builder.setItems(AccountListFragment.this.getResources().getStringArray(R.array.account_action_array), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        Intent intent = new Intent("com.x.wallet.action.COINTYPE_CHOOSE_ACTION");
                        switch (which){
                            case 0:
                                intent.putExtra(AppUtils.ACTION_TYPE, AppUtils.ACCOUNT_ACTION_TYPE_NEW);
                                break;
                            case 1:
                                intent.putExtra(AppUtils.ACTION_TYPE, AppUtils.ACCOUNT_ACTION_TYPE_IMPORT);
                                break;
                        }
                        AccountListFragment.this.startActivity(intent);
                    }
                });
                builder.setTitle(R.string.add_account);
                builder.show();
            }
        });
    }

    public void init(final LoaderManager loaderManager) {
        final Bundle args = new Bundle();
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(ACCOUNT_LIST_LOADER, args, new AccountListLoaderCallbacks());
    }

    private class AccountListLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            return new CursorLoader(AccountListFragment.this.getActivity(), XWalletProvider.CONTENT_URI,
                    null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.i("test4", "AccountListFragment onLoadFinished cursor.count = " + cursor.getCount());
            mAccountListAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
