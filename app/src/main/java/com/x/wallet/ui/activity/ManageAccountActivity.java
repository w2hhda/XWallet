package com.x.wallet.ui.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.ChangePasswordAsycTask;
import com.x.wallet.transaction.DeleteAccountAsycTask;
import com.x.wallet.transaction.key.DecryptKeyAsycTask;
import com.x.wallet.transaction.keystore.DecryptKeyStoreAsycTask;
import com.x.wallet.transaction.token.ManagerTokenListLoader;
import com.x.wallet.transaction.token.TokenListLoader;
import com.x.wallet.ui.adapter.ManagerTokenListAdapter;
import com.x.wallet.ui.adapter.RecyclerViewArrayAdapter;
import com.x.wallet.ui.data.AccountItem;
import com.x.wallet.ui.data.SerializableAccountItem;
import com.x.wallet.ui.data.TokenItem;
import com.x.wallet.ui.data.TokenItemBean;
import com.x.wallet.ui.dialog.ChangePasswordDialogHelper;
import com.x.wallet.ui.dialog.ContentShowDialogHelper;
import com.x.wallet.ui.dialog.PasswordCheckDialogHelper;

import java.util.List;

/**
 * Created by wuliang on 18-3-16.
 */

public class ManageAccountActivity extends WithBackAppCompatActivity {
    private SerializableAccountItem mAccountItem;
    private TextView mAccountNameTv;
    private TextView mAddressTv;

    private View mMnemonicView;
    private View mKeyView;
    private View mKeyStoreView;

    private View mChangePasswordView;

    private View mDeleteView;
    private Activity mActivity;
    private View managerTokenLayout;
    private RecyclerView recyclerView;
    private ManagerTokenListAdapter mAdapter;
    private LoaderManager mLoaderManager;
    private static final int TOKEN_LIST_LOADER = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_account_activity);
        mActivity = this;
        mAccountItem = (SerializableAccountItem) getIntent().getSerializableExtra(AppUtils.ACCOUNT_DATA);

        initViews();
        initRecyclerView();
        initLoaderManager();
    }

    private void initViews(){
        super.setTitle(getResources().getString(R.string.manage_account));
        managerTokenLayout = findViewById(R.id.manage_token_layout);
        mAccountNameTv = findViewById(R.id.account_name_tv);
        mAccountNameTv.setText(mAccountItem.getAccountName());

        mAddressTv = findViewById(R.id.address_tv);
        mAddressTv.setText(mAccountItem.getAddress());

        mMnemonicView = findViewById(R.id.mnemonic_tv);
        mMnemonicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = ContentUris.withAppendedId(XWalletProvider.CONTENT_URI, mAccountItem.getId());
                Intent intent = new Intent("com.x.wallet.action.BACKUP_MNEMONIC_ACTION");
                intent.putExtra(AppUtils.ADDRESS_URI, uri);
                startActivity(intent);
            }
        });
        mMnemonicView.setVisibility(mAccountItem.hasMnemonic() ? View.VISIBLE : View.GONE);

        mKeyView = findViewById(R.id.key_tv);
        mKeyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordCheckDialogForKey();
            }
        });

        mKeyStoreView = findViewById(R.id.keystore_tv);
        mKeyStoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordCheckDialogForKeyStore();
            }
        });

        if(mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_ETH){
            if(mAccountItem.hasKeyStore()){
                mKeyView.setVisibility(View.VISIBLE);
                mKeyStoreView.setVisibility(View.VISIBLE);
            } else {
                mKeyView.setVisibility(View.GONE);
                mKeyStoreView.setVisibility(View.GONE);
            }
        } else {
            mKeyView.setVisibility(mAccountItem.hasKey() ? View.VISIBLE : View.GONE);
            mKeyStoreView.setVisibility(View.GONE);
        }

        mChangePasswordView = findViewById(R.id.change_password_tv);
        mChangePasswordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePasswordDialog();
            }
        });

        mDeleteView = findViewById(R.id.delete_account_tv);
        mDeleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordCheckDialogForDelete();
            }
        });
    }

    private void initRecyclerView(){
        recyclerView = findViewById(R.id.recyclerView_manage_token);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
        MyHandler handler = new MyHandler();
        mAdapter = new ManagerTokenListAdapter(R.layout.manager_token_list_item, mAccountItem.getAddress(), handler);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    /**
     * BtcCreateAddressHelper.readMnemonic(mAccountItem.getEncryMnemonic(), passwordEt.getText().toString()):
     BtcCreateAddressHelper.readPrivateKey(mAccountItem.getEncrySeed(), passwordEt.getText().toString());
     */

    private void showPasswordCheckDialogForKey(){
        final PasswordCheckDialogHelper mPasswordCheckDialogHelper = new PasswordCheckDialogHelper();

        mPasswordCheckDialogHelper.showPasswordDialog(this, new PasswordCheckDialogHelper.ConfirmBtnClickListener() {
            @Override
            public void onConfirmBtnClick(String password, Context context) {
                new DecryptKeyAsycTask(context, mAccountItem.getCoinType(), "", mAccountItem.getKeyStore(),
                        password, new DecryptKeyAsycTask.OnDecryptKeyFinishedListener() {
                    @Override
                    public void onDecryptKeyFinished(String key) {
                        if(!TextUtils.isEmpty(key)){
                            mPasswordCheckDialogHelper.dismissDialog();
                            ContentShowDialogHelper.showContentDialog(mActivity, R.string.backup_key, R.string.copy_key, key);
                        } else {
                            mPasswordCheckDialogHelper.updatePasswordCheckError();
                        }
                    }
                }).execute();
            }
        }, R.string.confirm_password);
    }

    private void showPasswordCheckDialogForKeyStore(){
        final PasswordCheckDialogHelper mPasswordCheckDialogHelper = new PasswordCheckDialogHelper();

        mPasswordCheckDialogHelper.showPasswordDialog(this, new PasswordCheckDialogHelper.ConfirmBtnClickListener() {
            @Override
            public void onConfirmBtnClick(String password, Context context) {
                new DecryptKeyStoreAsycTask(context, mAccountItem.getCoinType(), mAccountItem.getKeyStore(),
                        password, new DecryptKeyStoreAsycTask.OnDecryptKeyStoreFinishedListener() {
                    @Override
                    public void onDecryptKeyStoreFinished(String keyStore) {
                        if(!TextUtils.isEmpty(keyStore)){
                            mPasswordCheckDialogHelper.dismissDialog();
                            ContentShowDialogHelper.showContentDialog(mActivity, R.string.backup_keystore, R.string.copy_keystore, keyStore);
                        } else {
                            mPasswordCheckDialogHelper.updatePasswordCheckError();
                        }
                    }
                }).execute();
            }
        }, R.string.confirm_password);
    }

    private void showPasswordCheckDialogForDelete(){
        final PasswordCheckDialogHelper passwordCheckDialogHelper = new PasswordCheckDialogHelper();

        passwordCheckDialogHelper.showPasswordDialog(this, new PasswordCheckDialogHelper.ConfirmBtnClickListener() {
            @Override
            public void onConfirmBtnClick(String password, Context context) {
                new DecryptKeyStoreAsycTask(context, mAccountItem.getCoinType(), mAccountItem.getKeyStore(),
                        password, new DecryptKeyStoreAsycTask.OnDecryptKeyStoreFinishedListener() {
                    @Override
                    public void onDecryptKeyStoreFinished(String keyStore) {
                        if(!TextUtils.isEmpty(keyStore)){
                            passwordCheckDialogHelper.dismissDialog();
                            ContentShowDialogHelper.showConfirmDialog(mActivity, R.string.delete_account
                                    , mActivity.getResources().getString(R.string.confirm_delete_account, mAccountItem.getAccountName())
                                    , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    new DeleteAccountAsycTask(mActivity, mAccountItem.getId(), new DeleteAccountAsycTask.OnDeleteFinishedListener() {
                                        @Override
                                        public void onDeleteFinished(int count) {
                                            if(count > 0){
                                                Toast.makeText(mActivity, R.string.delete_account_success, Toast.LENGTH_LONG).show();
                                                mActivity.finish();
                                            } else {
                                                Toast.makeText(mActivity, R.string.delete_account_failed, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }).execute();
                                }
                            });
                        } else {
                            passwordCheckDialogHelper.updatePasswordCheckError();
                        }
                    }
                }).execute();
            }
        }, R.string.confirm_password);
    }


    private void showChangePasswordDialog(){
        final ChangePasswordDialogHelper changePasswordDialogHelper = new ChangePasswordDialogHelper();

        changePasswordDialogHelper.showPasswordDialog(this, new ChangePasswordDialogHelper.ConfirmBtnClickListener() {
            @Override
            public void onConfirmBtnClick(String oldPassword, String newPassword) {
                new ChangePasswordAsycTask(mActivity, mAccountItem.getId(), oldPassword,
                        newPassword, new ChangePasswordAsycTask.OnChangePasswordFinishedListener() {
                    @Override
                    public void onChangePasswordFinished(ChangePasswordAsycTask.ChangePasswordResult result) {
                        if(result.isSuccess()){
                            mAccountItem.setEncryMnemonic(result.getEncrptMnemonic());
                            mAccountItem.setKeyStore(result.getKeyStore());
                            Toast.makeText(mActivity, R.string.change_password_success, Toast.LENGTH_LONG).show();
                            changePasswordDialogHelper.dismissDialog();
                        } else {
                            Toast.makeText(mActivity, R.string.change_password_failed, Toast.LENGTH_LONG).show();
                            changePasswordDialogHelper.updatePasswordCheckError();
                        }
                    }
                }).execute();
            }
        }, R.string.change_password);
    }
    public class MyHandler extends Handler {
        public static final int MSG_UPDATE = 200;

        @Override
        public void handleMessage(Message msg) {
            Log.i(AppUtils.APP_TAG, "ManagerAccountActivity.MyHandler: msg.what = " + msg.what);
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE:
                    updateTokenLayoutVisibility(mAdapter.getItemCount());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoaderManager != null){
            mLoaderManager.destroyLoader(TOKEN_LIST_LOADER);
            mLoaderManager = null;
        }
    }

    private void initLoaderManager(){
        mLoaderManager = getLoaderManager();
        Loader tokenListLoader = getLoaderManager().initLoader(
                TOKEN_LIST_LOADER,
                null,
                mLoaderCallbacks);
        tokenListLoader.forceLoad();
    }

    private void updateTokenLayoutVisibility(int count){
        managerTokenLayout.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
    }

    private final LoaderManager.LoaderCallbacks<List<TokenItem>> mLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<TokenItem>>() {

                @Override
                public Loader<List<TokenItem>> onCreateLoader(int id, Bundle args) {
                    return new ManagerTokenListLoader(ManageAccountActivity.this, mAccountItem.getAddress());
                }

                @Override
                public void onLoadFinished(Loader<List<TokenItem>> loader, List<TokenItem> tokenItems) {
                    mAdapter.addAll(tokenItems);
                    updateTokenLayoutVisibility(mAdapter.getItemCount());
                }

                @Override
                public void onLoaderReset(Loader<List<TokenItem>> loader) {

                }
            };
}
