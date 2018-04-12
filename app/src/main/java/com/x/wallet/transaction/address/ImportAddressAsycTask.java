package com.x.wallet.transaction.address;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.AccountData;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.transaction.balance.TokenListBean;
import com.x.wallet.transaction.token.ReadFileUtils;
import com.x.wallet.transaction.token.TokenDeserializer;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by wuliang on 18-3-15.
 */

public class ImportAddressAsycTask extends AsyncTask<Void, Void, Integer>{
    private int mCoinType;
    private int mImportType;
    private String mPassword;
    private String mAccountName;

    private int mMnemonicType;
    private String mMnemonic;

    private String mKey;

    private String mKeyStore;
    private String mKeyStorePassword;

    private ProgressDialog mProgressDialog;
    private Context mContext;

    private Uri mUri = null;

    public ImportAddressAsycTask(Context context, int importType, int coinType, String password, String accountName) {
        mContext = context;
        mCoinType = coinType;
        mImportType = importType;
        mPassword = password;
        mAccountName = accountName;
        mProgressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        int resultType = AppUtils.CREATE_ADDRESS_FAILED_OTHER;
        boolean isAccountNameExist = DbUtils.isAccountNameExist(mAccountName);
        if(isAccountNameExist){
            resultType = AppUtils.CREATE_ADDRESS_FAILED_ACCOUNTNAME_SAME;
        } else {
            AccountData data = null;
            switch (mImportType){
                case AppUtils.IMPORTTYPE.IMPORT_TYPE_MNEMONIC:
                    data = AddressUtils.importAddressThroughMnemonic(mCoinType,
                            mPassword,
                            mAccountName,
                            mMnemonicType,
                            mMnemonic);
                    break;
                case AppUtils.IMPORTTYPE.IMPORT_TYPE_KEY:
                    data = AddressUtils.importAddressThroughKey(mCoinType,
                            mPassword,
                            mAccountName,
                            mKey);
                    break;
                case AppUtils.IMPORTTYPE.IMPORT_TYPE_KEYSTORE:
                    data = AddressUtils.importAddressThroughKeyStore(mCoinType,
                            mPassword,
                            mAccountName, mKeyStore, mKeyStorePassword);
                    break;
            }
            if(data != null){
                boolean isAddressExist = DbUtils.isAddressExist(data.getAddress());
                if(isAddressExist){
                    resultType = AppUtils.CREATE_ADDRESS_FAILED_ADDRESS_EXIST;
                } else {
                    mUri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, DbUtils.createContentValues(data));
                    if(mUri != null){
                        resultType = AppUtils.CREATE_ADDRESS_OK;
                        long accountId = ContentUris.parseId(mUri);
                        requestBalanceForToken(data, accountId);
                    }
                }
            }
        }
         return resultType;
    }

    @Override
    protected void onPostExecute(Integer resultType) {
        mProgressDialog.dismiss();
        switch (resultType){
            case AppUtils.CREATE_ADDRESS_OK:
                Toast.makeText(mContext, R.string.import_address_success, Toast.LENGTH_LONG).show();
                if(mContext instanceof Activity){
                    ((Activity) mContext).finish();
                }
                break;
            case AppUtils.CREATE_ADDRESS_FAILED_OTHER:
                Toast.makeText(mContext, R.string.import_address_failed, Toast.LENGTH_LONG).show();
                break;
            case AppUtils.CREATE_ADDRESS_FAILED_ACCOUNTNAME_SAME:
                Toast.makeText(mContext, R.string.create_address_failed_accountname_same, Toast.LENGTH_LONG).show();
                break;
            case AppUtils.CREATE_ADDRESS_FAILED_ADDRESS_EXIST:
                Toast.makeText(mContext, R.string.create_address_failed_address_exist, Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void setMnemonic(String mnemonic) {
        this.mMnemonic = mnemonic;
    }

    public void setKeyStore(String keyStore, String keyStorePassword) {
        mKeyStore = keyStore;
        mKeyStorePassword = keyStorePassword;
    }

    public void setKey(String key) {
        mKey = key;
    }

    private void insertTokenIntoDb(String address, List<TokenListBean.TokenBean> tokens, String accountId) {
        for (TokenListBean.TokenBean token : tokens) {
            String mHasToken = "0";
            TokenListBean.TokenInfo tokenInfo = token.getTokenInfo();
            boolean isExist = DbUtils.isAlreadyExistToken(DbUtils.UPDATE_TOKEN_SELECTION, new String[]{address, tokenInfo.getSymbol()});
            Log.i(AppUtils.APP_TAG, "insertTokenIntoDb when import isExist = " + isExist);
            if (isExist) {
                continue;
            }
            String symbol = null;
            int decimals = 1;
            double rate = 0;
            try {
                if (tokenInfo != null){
                    symbol = tokenInfo.getSymbol();
                    decimals = tokenInfo.getDecimals();
                    if (tokenInfo.getPrice() != null){
                        rate = tokenInfo.getPrice().getRate();
                    }
                }
            }catch (JsonSyntaxException | IllegalStateException e){
                Log.e(AppUtils.APP_TAG, "try to insert illegal token, just ignore");
                continue;
            }

            ContentValues values = new ContentValues();
            values.put(DbUtils.TokenTableColumns.ACCOUNT_ID, accountId);
            values.put(DbUtils.TokenTableColumns.ACCOUNT_ADDRESS, address);
            values.put(DbUtils.TokenTableColumns.ID_IN_ALL, tokens.indexOf(token));
            values.put(DbUtils.TokenTableColumns.NAME, tokenInfo.getName());
            values.put(DbUtils.TokenTableColumns.SYMBOL, symbol);
            values.put(DbUtils.TokenTableColumns.DECIMALS, decimals);
            values.put(DbUtils.TokenTableColumns.CONTRACT_ADDRESS, tokenInfo.getAddress());
            values.put(DbUtils.TokenTableColumns.BALANCE, token.getBalance());
            values.put(DbUtils.TokenTableColumns.RATE, rate);
            Uri uri = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                    .insert(XWalletProvider.CONTENT_URI_TOKEN, values);

            if (mHasToken.equals(0)) {
                ContentValues updateValues = new ContentValues();
                updateValues.put(DbUtils.DbColumns.HAS_TOKEN, AppUtils.HAS_TOKEN);
                int count = XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                        .update(XWalletProvider.CONTENT_URI, updateValues,
                                DbUtils.DbColumns._ID + " = ?",
                                new String[]{String.valueOf(accountId)});
                Log.i(AppUtils.APP_TAG, "InsertTokenIntoDb when import count = " + count + ", mAccountId = " + accountId);
            }
            Log.i(AppUtils.APP_TAG, "InsertTokenIntoDb when import uri = " + uri);

        }
    }

    private void requestBalanceForToken(final AccountData data, final long accountId){
        final String address = data.getAddress();
        Log.i(AppUtils.APP_TAG, "ImportAddressAsyncTask requestBalanceForToken address = " + address);
        try{
            EtherscanAPI.getInstance().getTokenBalances(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(AppUtils.APP_TAG, "ImportAddressAsyncTask requestBalanceForToken onFailure address = " + address, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try{
                        ResponseBody body = response.body();
                        if(body != null){
                            String result = body.string();
                            Log.i(AppUtils.APP_TAG, "ImportAddressAsyncTask requestBalanceForToken result = " + result);

                            //1.parse
                            List<TokenListBean.TokenBean> tokens = parseTokenJson(result);
                            if(tokens == null || tokens.size() <= 0){
                                Log.i(AppUtils.APP_TAG, "ImportAddressAsyncTask requestBalanceForToken no tokens");
                                return;
                            }

                            //2.insert into db
                            insertTokenIntoDb(address, tokens, Long.toString(accountId));
                        }
                    } catch (Exception e){
                        Log.e(AppUtils.APP_TAG, "ImportAddressAsyncTask requestBalanceForToken onResponse exception1 address = " + address, e);
                    } finally {
                        if(response != null){
                            response.close();
                        }

                    }
                }
            }, true);
        } catch (Exception e){
            Log.e(AppUtils.APP_TAG, "BalanceLoaderManager requestBalanceForToken exception2", e);
        }
    }

    private List<TokenListBean.TokenBean> parseTokenJson(String result){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TokenListBean.class, new TokenDeserializer());
        Gson gson = gsonBuilder.create();
        TokenListBean tokenListBean = gson.fromJson(result, TokenListBean.class);
        return tokenListBean.getTokens();
    }
}
