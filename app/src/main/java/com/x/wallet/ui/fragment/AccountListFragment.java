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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.x.wallet.transaction.balance.BalanceLoaderManager;
import com.x.wallet.transaction.balance.ItemLoadedCallback;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;
import com.x.wallet.ui.ActionUtils;
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

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private MenuItem mAddItem;
    private MenuItem mImportItem;

    private static final int ACCOUNT_LIST_LOADER = 1;
    private static final int ALL_BALANCE_LOADER = 2;

    private AllBalanceLoader mAllBalanceLoader;
    private int mDataCount = -1;

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
                    final RawAccountItem rawAccountItem = new RawAccountItem(accountItem.getCoinName(), accountItem.getBalance(),
                            accountItem.getDecimals(), accountItem.getRate(), accountItem.getContractAddress());
                    intent.putExtra(AppUtils.TOKEN_DATA, rawAccountItem);
                }
                getContext().startActivity(intent);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment, container, false);
        initViews(view);
        initRecyclerView(view);
        initSwipeRefreshLayout(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init(getLoaderManager());
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.add_acount_menu, menu);
//        super.onCreateOptionsMenu(menu,inflater);
//
//        mAddItem = menu.findItem(R.id.action_add_account);
//        mImportItem = menu.findItem(R.id.action_import_account);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.action_add_account:
//                handleAccountAction(AppUtils.ACCOUNT_ACTION_TYPE_NEW);
//                return true;
//            case R.id.action_import_account:
//                handleAccountAction(AppUtils.ACCOUNT_ACTION_TYPE_IMPORT);
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

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
                        ActionUtils.handleAddAccountAction(AccountListFragment.this.getContext(), which);
                    }
                });
                builder.setTitle(R.string.add_account);
                builder.show();
            }
        });
    }

    private void initSwipeRefreshLayout(View rootView){
        mSwipeRefreshLayout = rootView.findViewById(R.id.swiperefreshlayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestBalance(createItemLoadedCallback(), true);
            }
        });
        mSwipeRefreshLayout.setEnabled(false);
    }

    private void init(final LoaderManager loaderManager) {
        final Bundle args = new Bundle();
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(ACCOUNT_LIST_LOADER, args, new AccountListLoaderCallbacks());
        mAllBalanceLoader = (AllBalanceLoader) mLoaderManager.initLoader(ALL_BALANCE_LOADER, args, new AllBalanceLoaderCallbacks());
        mAllBalanceLoader.forceLoad();
    }

    private void updateViewVisibility(int dataCount){
        if(mDataCount != dataCount){
            mDataCount = dataCount;
            if(dataCount > 0){
                mAddAccountViewContainer.setVisibility(View.GONE);
                mEmptyAccountViewContainer.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mAllBalanceViewContainer.setVisibility(View.VISIBLE);
                updateMenuVisibility(true);
                mSwipeRefreshLayout.setEnabled(true);
                requestBalance(null, false);
            } else {
                mAddAccountViewContainer.setVisibility(View.VISIBLE);
                mEmptyAccountViewContainer.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                mAllBalanceViewContainer.setVisibility(View.GONE);
                updateMenuVisibility(false);
                mSwipeRefreshLayout.setEnabled(false);
            }
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
            mAllBalanceLoader.forceLoad();
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
            Log.i(AppUtils.APP_TAG, "AllBalanceLoader onLoadFinished allBalance = " + allBalance);
            mAllBalanceTv.setText(AccountListFragment.this.getActivity().getString(R.string.all_balance_prefix, UsdToCnyHelper.getChooseCurrencyUnit(),
                    allBalance));
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    }

    private ItemLoadedCallback createItemLoadedCallback(){
        return new ItemLoadedCallback<BalanceLoaderManager.BalanceLoaded>() {
            @Override
            public void onItemLoaded(BalanceLoaderManager.BalanceLoaded result, Throwable exception) {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        };
    }

    private void requestBalance(ItemLoadedCallback<BalanceLoaderManager.BalanceLoaded> callback, boolean isNeedAutoAddToken){
        XWalletApplication.getApplication().getBalanceLoaderManager().getAllBalance(callback, isNeedAutoAddToken);
    }
}
