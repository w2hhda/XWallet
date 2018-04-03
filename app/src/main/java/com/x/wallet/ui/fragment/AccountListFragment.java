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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.balance.AllBalanceLoader;
import com.x.wallet.transaction.balance.BalanceConversionUtils;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.ui.adapter.AccountListAdapter;
import com.x.wallet.ui.data.AllAccountItem;
import com.x.wallet.ui.data.RawAccountItem;
import com.x.wallet.ui.view.RawAccountListItem;

/**
 * Created by wuliang on 18-3-13.
 */

public class AccountListFragment extends Fragment {
    private AccountListAdapter mAccountListAdapter;
    private LoaderManager mLoaderManager;

    private View mAllBalanceViewContainer;
    private TextView mAllBalanceTv;
    private View mEmptyAccountViewContainer;

    private View mAddAccountViewContainer;
    private View mAddAccountView;

    private RecyclerView mRecyclerView;

    private MenuItem mAddItem;
    private MenuItem mImportItem;

    private static final int ACCOUNT_LIST_LOADER = 1;
    private static final int ALL_BALANCE_LOADER = 2;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAccountListAdapter = new AccountListAdapter(getActivity(), null, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final RawAccountListItem listItem = (RawAccountListItem) view;
                AllAccountItem accountItem = listItem.getAccountItem();
                Intent intent = new Intent("com.x.wallet.action.SEE_ACCOUNT_DETAIL_ACTION");
                intent.putExtra(AppUtils.ACCOUNT_DATA, AllAccountItem.translateToSerializable(accountItem));
                if(accountItem.isToken()){
                    AccountListFragment.this.getActivity().startActivity(intent);
                } else {
                    final RawAccountItem rawAccountItem = new RawAccountItem(accountItem.getCoinName(), accountItem.getIdInAll(), accountItem.getBalance(),
                            accountItem.getDecimals(), accountItem.getRate(), accountItem.getContractAddress());
                    intent.putExtra(AppUtils.TOKEN_DATA, rawAccountItem);
                    getContext().startActivity(intent);
                }
            }
        });

        XWalletApplication.getApplication().getBalanceLoaderManager().getBalance(null);
        XWalletApplication.getApplication().getBalanceLoaderManager().getAllTokenBalance(null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment, container, false);
        initViews(view);
        initRecyclerView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init(getLoaderManager());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_acount_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);

        mAddItem = menu.findItem(R.id.action_add_account);
        mImportItem = menu.findItem(R.id.action_import_account);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_account:
                handleAccountAction(AppUtils.ACCOUNT_ACTION_TYPE_NEW);
                return true;
            case R.id.action_import_account:
                handleAccountAction(AppUtils.ACCOUNT_ACTION_TYPE_IMPORT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BalanceConversionUtils.clearListener();
        TokenUtils.setRateUpdateListener(null);
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(ACCOUNT_LIST_LOADER);
            mLoaderManager.destroyLoader(ALL_BALANCE_LOADER);
            mLoaderManager = null;
        }
    }

    private void initRecyclerView(View rootView){
        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAccountListAdapter);
    }

    private void initViews(View rootView){
        mAllBalanceViewContainer = rootView.findViewById(R.id.all_balance_container);
        mAllBalanceTv = rootView.findViewById(R.id.all_balance_tv);
        mEmptyAccountViewContainer = rootView.findViewById(R.id.empty_ll);

        mAddAccountViewContainer = rootView.findViewById(R.id.add_account_ll);
        mAddAccountView = rootView.findViewById(R.id.add_account_btn);
        mAddAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountListFragment.this.getActivity());
                builder.setItems(AccountListFragment.this.getResources().getStringArray(R.array.account_action_array), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        handleAccountAction(which);
                    }
                });
                builder.setTitle(R.string.add_account);
                builder.show();
            }
        });
    }

    private void init(final LoaderManager loaderManager) {
        final Bundle args = new Bundle();
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(ACCOUNT_LIST_LOADER, args, new AccountListLoaderCallbacks());
        mLoaderManager.initLoader(ALL_BALANCE_LOADER, args, new AllBalanceLoaderCallbacks()).forceLoad();
    }

    private void updateViewVisibility(int dataCount){
        if(dataCount > 0){
            mAddAccountViewContainer.setVisibility(View.GONE);
            mEmptyAccountViewContainer.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mAllBalanceViewContainer.setVisibility(View.VISIBLE);
            updateMenuVisibility(true);
        } else {
            mAddAccountViewContainer.setVisibility(View.VISIBLE);
            mEmptyAccountViewContainer.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mAllBalanceViewContainer.setVisibility(View.GONE);
            updateMenuVisibility(false);
        }
    }

    private void updateMenuVisibility(boolean isVisible){
        if(mAddItem != null){
            mAddItem.setVisible(isVisible);
        }
        if(mImportItem != null){
            mImportItem.setVisible(isVisible);
        }
    }

    private class AccountListLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            return new CursorLoader(AccountListFragment.this.getActivity(), XWalletProvider.ALL_ACCOUNT_CONTENT_URI,
                    AllAccountItem.PROJECTION, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.i(AppUtils.APP_TAG, "AccountListFragment onLoadFinished cursor.count = " + cursor.getCount());
            updateViewVisibility(cursor.getCount());
            mAccountListAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private class AllBalanceLoaderCallbacks implements LoaderManager.LoaderCallbacks<String> {


        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            return new AllBalanceLoader(AccountListFragment.this.getActivity());
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String allBalance) {
            mAllBalanceTv.setText(AccountListFragment.this.getActivity().getString(R.string.all_balance, allBalance));
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    }

    private void handleAccountAction(int actionType){
//        Intent intent = new Intent("com.x.wallet.action.COINTYPE_CHOOSE_ACTION");
//        intent.putExtra(AppUtils.ACTION_TYPE, actionType);
//        AccountListFragment.this.startActivity(intent);
//        AccountListFragment.this.startActivity(intent);

        if(actionType == AppUtils.ACCOUNT_ACTION_TYPE_NEW){
            Intent intent = new Intent("com.x.wallet.action.CREATE_ACCOUNT_ACTION");
            intent.putExtra(AppUtils.COIN_TYPE, LibUtils.COINTYPE.COIN_ETH);
            startActivity(intent);
        } else {
            Intent intent = new Intent("com.x.wallet.action.IMPORT_ACCOUNT_ACTION");
            intent.putExtra(AppUtils.COIN_TYPE, LibUtils.COINTYPE.COIN_ETH);
            startActivity(intent);
        }
    }
}
