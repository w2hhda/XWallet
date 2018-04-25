package com.x.wallet.transaction.address;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.XWalletApplication;
import com.x.wallet.btc.BtcUtils;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.AccountData;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.transaction.balance.RetrofitClient;
import com.x.wallet.transaction.balance.TokenListBean;

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
                    BtcUtils.stopPeer(mCoinType);
                    mUri = XWalletApplication.getApplication().getContentResolver().insert(XWalletProvider.CONTENT_URI, DbUtils.createContentValues(data));
                    if(mUri != null){
                        resultType = AppUtils.CREATE_ADDRESS_OK;
                        if(mCoinType == LibUtils.COINTYPE.COIN_ETH){
                            long accountId = ContentUris.parseId(mUri);
                            requestBalanceForToken(data, accountId);
                        }
                    }
                    BtcUtils.startPeer(mCoinType);
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
                            List<TokenListBean.TokenBean> tokens = RetrofitClient.parseTokenJson(result).getTokens();
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

    private void insertTokenIntoDb(String address, List<TokenListBean.TokenBean> tokens, String accountId) {
        for (TokenListBean.TokenBean token : tokens) {
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

            Uri uri = DbUtils.insertTokenIntoDb(Long.parseLong(accountId), address,
                    tokenInfo.getName(), symbol, decimals,
                    tokenInfo.getAddress(), token.getBalance(), String.valueOf(rate));
            Log.i(AppUtils.APP_TAG, "InsertTokenIntoDb when import uri = " + uri);
        }
    }
}
