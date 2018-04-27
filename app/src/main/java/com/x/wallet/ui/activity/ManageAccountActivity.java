package com.x.wallet.ui.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.ChangePasswordAsycTask;
import com.x.wallet.transaction.DeleteAccountAsycTask;
import com.x.wallet.transaction.key.DecryptKeyAsycTask;
import com.x.wallet.transaction.keystore.DecryptKeyStoreAsycTask;
import com.x.wallet.ui.data.SerializableAccountItem;
import com.x.wallet.ui.dialog.ChangePasswordDialogHelper;
import com.x.wallet.ui.dialog.ContentShowDialogHelper;
import com.x.wallet.ui.dialog.PasswordCheckDialogHelper;
import com.x.wallet.ui.view.ManageTokenView;

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
    private View mDeleteNoticeTv;
    private Activity mActivity;
    private ManageTokenView mManagerTokenLayout;

    private final int ACCOUNT_ITEM_LOADER = 2;
    private LoaderManager mLoaderManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_account_activity);
        mActivity = this;

        init();
        initViews();
        initTokenRelatedViews();
    }

    private void initViews(){
        mAccountNameTv = findViewById(R.id.account_name_tv);
        mAccountNameTv.setText(mAccountItem.getAccountName());

        mAddressTv = findViewById(R.id.address_tv);
        mAddressTv.setText(getResources().getString(R.string.address, mAccountItem.getAddress()));
        mAddressTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.copyContent(ManageAccountActivity.this, R.string.has_copied_address, mAccountItem.getAddress(), "");
            }
        });


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
            mKeyView.setVisibility(mAccountItem.hasKey() || mAccountItem.hasMnemonic() ? View.VISIBLE : View.GONE);
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
        mDeleteNoticeTv = findViewById(R.id.delete_account_notice_tv);
        updateDeleteEnable();
    }

    private void initTokenRelatedViews(){
        if(mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_ETH){
            mManagerTokenLayout = (ManageTokenView) AppUtils.getStubView(this, R.id.manage_account_token_related_view_stub, R.id.manage_token_layout);
            mManagerTokenLayout.setVisibility(View.VISIBLE);
            mManagerTokenLayout.init(getLoaderManager(), mAccountItem.getId(), mAccountItem.getAddress());
        }
    }

    private void showPasswordCheckDialogForKey(){
        final PasswordCheckDialogHelper mPasswordCheckDialogHelper = new PasswordCheckDialogHelper();

        mPasswordCheckDialogHelper.showPasswordDialog(this, new PasswordCheckDialogHelper.ConfirmBtnClickListener() {
            @Override
            public void onConfirmBtnClick(String password, Context context) {
                new DecryptKeyAsycTask(context, mAccountItem.getCoinType(), password, mAccountItem.getKeyStore(),
                        mAccountItem.getEncrySeed(), mAccountItem.getPrivKey(),
                        new DecryptKeyAsycTask.OnDecryptKeyFinishedListener() {
                    @Override
                    public void onDecryptKeyFinished(String key) {
                        if(!TextUtils.isEmpty(key)){
                            mPasswordCheckDialogHelper.dismissDialog();
                            ContentShowDialogHelper.showContentDialog(mActivity, R.string.backup_key, R.string.copy_key, key, mAccountItem.getId());
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
                            ContentShowDialogHelper.showContentDialog(mActivity, R.string.backup_keystore, R.string.copy_keystore, keyStore,  mAccountItem.getId());
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
                if(mAccountItem.getCoinType() == LibUtils.COINTYPE.COIN_ETH){
                    new DecryptKeyStoreAsycTask(context, mAccountItem.getCoinType(), mAccountItem.getKeyStore(),
                            password, new DecryptKeyStoreAsycTask.OnDecryptKeyStoreFinishedListener() {
                        @Override
                        public void onDecryptKeyStoreFinished(String keyStore) {
                            if(!TextUtils.isEmpty(keyStore)){
                                passwordCheckDialogHelper.dismissDialog();
                                showConfirmDialog();
                            } else {
                                passwordCheckDialogHelper.updatePasswordCheckError();
                            }
                        }
                    }).execute();
                } else {
                    new DecryptKeyAsycTask(context, mAccountItem.getCoinType(), password, mAccountItem.getKeyStore(),
                            mAccountItem.getEncrySeed(), mAccountItem.getPrivKey(),
                            new DecryptKeyAsycTask.OnDecryptKeyFinishedListener() {
                                @Override
                                public void onDecryptKeyFinished(String key) {
                                    if(!TextUtils.isEmpty(key)){
                                        passwordCheckDialogHelper.dismissDialog();
                                        showConfirmDialog();
                                    } else {
                                        passwordCheckDialogHelper.updatePasswordCheckError();
                                    }
                                }
                            }).execute();
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoaderManager != null){
            mLoaderManager.destroyLoader(ManageTokenView.TOKEN_LIST_LOADER);
            mLoaderManager.destroyLoader(ACCOUNT_ITEM_LOADER);
            mLoaderManager = null;
        }
    }

    private void init() {
        mAccountItem = (SerializableAccountItem) getIntent().getSerializableExtra(AppUtils.ACCOUNT_DATA);

        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(ACCOUNT_ITEM_LOADER, null, new AccountItemLoaderCallbacks());
    }

    private void updateDeleteEnable(){
        if(mAccountItem.isHasBackup()){
            mDeleteView.setEnabled(true);
            mDeleteNoticeTv.setVisibility(View.GONE);
        } else {
            mDeleteView.setEnabled(false);
            mDeleteNoticeTv.setVisibility(View.VISIBLE);
        }
    }

    private void showConfirmDialog(){
        ContentShowDialogHelper.showConfirmDialog(mActivity, R.string.delete_account
                , mActivity.getResources().getString(R.string.confirm_delete_account, mAccountItem.getAccountName())
                , null,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DeleteAccountAsycTask(mActivity, mAccountItem.getId(), mAccountItem.getAddress(), mAccountItem.getCoinType(),
                                new DeleteAccountAsycTask.OnDeleteFinishedListener() {
                                    @Override
                                    public void onDeleteFinished(int count) {
                                        if (count > 0) {
                                            Toast.makeText(mActivity, R.string.delete_account_success, Toast.LENGTH_LONG).show();
                                            mActivity.finish();
                                        } else {
                                            Toast.makeText(mActivity, R.string.delete_account_failed, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }).execute();
                    }
                });
    }

    public String[] mProjection = {DbUtils.DbColumns.ENCRYPT_SEED,         //0
                                    DbUtils.DbColumns.ENCRYPT_MNEMONIC,    //1
                                    DbUtils.DbColumns.ENCRYPT_PRIV_KEY,    //2
                                    DbUtils.DbColumns.KEYSTORE,            //3
                                    DbUtils.DbColumns.HAS_BACKUP};         //4

    private class AccountItemLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            return new CursorLoader(ManageAccountActivity.this, XWalletProvider.CONTENT_URI,
                    mProjection, DbUtils.ADDRESS_SELECTION, new String[]{mAccountItem.getAddress()}, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if(mAccountItem != null && cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                mAccountItem.setEncrySeed(cursor.getString(0));
                mAccountItem.setEncryMnemonic(cursor.getString(1));
                mAccountItem.setPrivKey(cursor.getString(2));
                mAccountItem.setKeyStore(cursor.getString(3));
                mAccountItem.setHasBackup(cursor.getInt(4) == AppUtils.HAS_BACKUP);
                updateDeleteEnable();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
