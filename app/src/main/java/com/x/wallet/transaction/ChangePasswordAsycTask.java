package com.x.wallet.transaction;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.lib.eth.api.EthAccountCreateHelper;
import com.x.wallet.transaction.ChangePasswordAsycTask.ChangePasswordResult;

import net.bither.bitherj.core.BtcCreateAddressHelper;
import net.bither.bitherj.crypto.EncryptedData;


/**
 * Created by wuliang on 18-3-15.
 */

public class ChangePasswordAsycTask extends AsyncTask<Void, Void, ChangePasswordResult> {
    private Context mContext;
    private long mAccountId;
    private String mNewPassword;
    private String mOldPassword;
    private OnChangePasswordFinishedListener mOnChangePasswordFinishedListener;

    private ProgressDialog mProgressDialog;

    public ChangePasswordAsycTask(Context context, long accountId, String oldPassword, String newPassword, OnChangePasswordFinishedListener listener) {
        mContext = context;
        mAccountId = accountId;
        mOldPassword = oldPassword;
        mNewPassword = newPassword;
        mOnChangePasswordFinishedListener = listener;
        mProgressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected ChangePasswordResult doInBackground(Void... voids) {
        ChangePasswordResult result = new ChangePasswordResult(false);
        Uri uri = ContentUris.withAppendedId(XWalletProvider.CONTENT_URI, mAccountId);
        Cursor cursor = null;
        try {
            cursor = XWalletApplication.getApplication().getApplicationContext().getContentResolver().query(
                    uri, PROJECTION, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int coinType = cursor.getInt(COLUMN_COIN_TYPE);
                String newEncryptHdSeed = null;
                String newEncryptMnemonic = null;
                String newPrivKey = null;
                String newKeyStore = null;

                String encryptHdSeed = cursor.getString(COLUMN_COIN_SEED);
                String encryptMnemonic = cursor.getString(COLUMN_COIN_MNEMONIC);
                String privKey = cursor.getString(COLUMN_RPIV_KEY);
                String keystore = cursor.getString(COLUMN_KEYSTORE);
                result.setEncryptHdSeed(encryptHdSeed);
                result.setEncrptMnemonic(encryptMnemonic);
                result.setEncryptPrivKey(privKey);
                result.setKeyStore(keystore);

                if (coinType == LibUtils.COINTYPE.COIN_ETH) {
                    ContentValues values = new ContentValues();
                    boolean isSeedDecryptOk = true;
                    if (!TextUtils.isEmpty(encryptHdSeed)) {
                        newEncryptHdSeed = EncryptedData.changePwd(encryptHdSeed, mOldPassword, mNewPassword);
                        isSeedDecryptOk = !TextUtils.isEmpty(newEncryptHdSeed);
                        values.put(DbUtils.DbColumns.ENCRYPT_SEED, newEncryptHdSeed);
                        result.setEncryptHdSeed(newEncryptHdSeed);
                    }
                    if (!isSeedDecryptOk) {
                        return result;
                    }

                    boolean isMnemonicDecryptOk = true;
                    if (!TextUtils.isEmpty(encryptMnemonic)) {
                        byte[] mnemonicSeed = BtcCreateAddressHelper.decryptMnemonicSeed(encryptMnemonic, mOldPassword);
                        if (mnemonicSeed != null) {
                            newEncryptMnemonic = BtcCreateAddressHelper.encryptMnemonicSeed(mnemonicSeed, mNewPassword);
                            values.put(DbUtils.DbColumns.ENCRYPT_MNEMONIC, newEncryptMnemonic);
                            result.setEncrptMnemonic(newEncryptMnemonic);
                        } else {
                            isMnemonicDecryptOk = false;
                        }
                    }

                    if (!isMnemonicDecryptOk) return result;

                    boolean isPrivKeyDecryptOk = true;
                    if (!TextUtils.isEmpty(privKey)) {
                        String rawPrivKey = EthAccountCreateHelper.decryptPrivKey(privKey, mOldPassword);
                        if (!TextUtils.isEmpty(rawPrivKey)) {
                            newPrivKey = EthAccountCreateHelper.encryptPrivKey(rawPrivKey, mNewPassword);
                            values.put(DbUtils.DbColumns.ENCRYPT_PRIV_KEY, newPrivKey);
                            result.setEncryptPrivKey(newPrivKey);
                        } else {
                            isPrivKeyDecryptOk = false;
                        }
                    }

                    if (!isPrivKeyDecryptOk) return result;

                    boolean isKeyStoreDecryptOk = true;
                    if (!TextUtils.isEmpty(keystore)) {
                        String oldKeyStore = EthAccountCreateHelper.checkPasswordForKeyStore(keystore, mOldPassword);
                        if (!TextUtils.isEmpty(oldKeyStore)) {
                            newKeyStore = EthAccountCreateHelper.generateKeyStoreWithNewPassword(keystore, mOldPassword, mNewPassword);
                            values.put(DbUtils.DbColumns.ENCRYPT_PRIV_KEY, newKeyStore);
                            result.setKeyStore(newKeyStore);
                        } else {
                            isKeyStoreDecryptOk = false;
                        }
                    }

                    if (!isKeyStoreDecryptOk) return result;

                    int count = XWalletApplication.getApplication().getApplicationContext().getContentResolver().update(uri, values, null, null);
                    result.setSuccess(count > 0);
                    return result;
                } else if (coinType == LibUtils.COINTYPE.COIN_BTC) {
                    ContentValues values = new ContentValues();
                    boolean isSeedDecryptOk = true;
                    if (!TextUtils.isEmpty(encryptHdSeed)) {
                        newEncryptHdSeed = EncryptedData.changePwd(encryptHdSeed, mOldPassword, mNewPassword);
                        isSeedDecryptOk = !TextUtils.isEmpty(newEncryptHdSeed);
                        values.put(DbUtils.DbColumns.ENCRYPT_SEED, newEncryptHdSeed);
                        result.setEncryptHdSeed(newEncryptHdSeed);
                    }
                    if (!isSeedDecryptOk) {
                        return result;
                    }

                    boolean isMnemonicDecryptOk = true;
                    if (!TextUtils.isEmpty(encryptMnemonic)) {
                        newEncryptMnemonic = EncryptedData.changePwd(encryptMnemonic, mOldPassword, mNewPassword);
                        isMnemonicDecryptOk = !TextUtils.isEmpty(newEncryptMnemonic);
                        values.put(DbUtils.DbColumns.ENCRYPT_MNEMONIC, newEncryptMnemonic);
                        result.setEncrptMnemonic(newEncryptMnemonic);
                    }

                    if (!isMnemonicDecryptOk) return result;

                    boolean isPrivKeyDecryptOk = true;
                    if (!TextUtils.isEmpty(privKey)) {
                        newPrivKey = EncryptedData.changePwd(privKey, mOldPassword, mNewPassword);
                        isPrivKeyDecryptOk = !TextUtils.isEmpty(newPrivKey);
                        values.put(DbUtils.DbColumns.ENCRYPT_PRIV_KEY, newPrivKey);
                        result.setEncryptPrivKey(newPrivKey);
                    }

                    if (!isPrivKeyDecryptOk) return result;

                    int count = XWalletApplication.getApplication().getApplicationContext().getContentResolver().update(uri, values, null, null);
                    result.setSuccess(count > 0);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(ChangePasswordResult result) {
        mProgressDialog.dismiss();
        if (mOnChangePasswordFinishedListener != null) {
            mOnChangePasswordFinishedListener.onChangePasswordFinished(result);
        }
    }

    public interface OnChangePasswordFinishedListener {
        void onChangePasswordFinished(ChangePasswordResult result);
    }

    private String[] PROJECTION = {
            DbUtils.DbColumns.COIN_TYPE,
            DbUtils.DbColumns.ENCRYPT_SEED,
            DbUtils.DbColumns.ENCRYPT_MNEMONIC,
            DbUtils.DbColumns.ENCRYPT_PRIV_KEY,
            DbUtils.DbColumns.KEYSTORE
    };

    static final int COLUMN_COIN_TYPE = 0;
    static final int COLUMN_COIN_SEED = 1;
    static final int COLUMN_COIN_MNEMONIC = 2;
    static final int COLUMN_RPIV_KEY = 3;
    static final int COLUMN_KEYSTORE = 4;

    public class ChangePasswordResult {
        boolean isSuccess;
        String mEncryptHdSeed;
        String mEncrptMnemonic;
        String mEncryptPrivKey;
        String mKeyStore;

        public ChangePasswordResult(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }

        public void setEncryptHdSeed(String encryptHdSeed) {
            mEncryptHdSeed = encryptHdSeed;
        }

        public String getEncryptHdSeed() {
            return mEncryptHdSeed;
        }

        public void setEncrptMnemonic(String encrptMnemonic) {
            mEncrptMnemonic = encrptMnemonic;
        }

        public void setEncryptPrivKey(String encryptPrivKey) {
            mEncryptPrivKey = encryptPrivKey;
        }

        public void setKeyStore(String keyStore) {
            mKeyStore = keyStore;
        }

        public String getEncrptMnemonic() {
            return mEncrptMnemonic;
        }

        public String getEncryptPrivKey() {
            return mEncryptPrivKey;
        }

        public String getKeyStore() {
            return mKeyStore;
        }
    }
}
