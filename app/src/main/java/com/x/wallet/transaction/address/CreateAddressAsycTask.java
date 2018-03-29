package com.x.wallet.transaction.address;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.db.DbUtils;

/**
 * Created by wuliang on 18-3-15.
 */

public class CreateAddressAsycTask extends AsyncTask<Void, Void, Integer>{
    private int mCoinType;
    private String mPassword;
    private String mAccountName;

    private ProgressDialog mProgressDialog;
    private Context mContext;
    private Uri mUri;

    public CreateAddressAsycTask(Context context, int coinType, String password, String accountName) {
        mCoinType = coinType;
        mPassword = password;
        mAccountName = accountName;
        mProgressDialog = new ProgressDialog(context);
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        int resultType = AppUtils.CREATE_ADDRESS_FAILED_OTHER;
        boolean result = DbUtils.isAccountNameExist(mAccountName);
        if(result){
            resultType = AppUtils.CREATE_ADDRESS_FAILED_ACCOUNTNAME_SAME;
        } else {
            mUri = AddressUtils.createAddress(mCoinType,
                    mPassword,
                    mAccountName);
            if(mUri != null){
                resultType = AppUtils.CREATE_ADDRESS_OK;
            }
        }
        return resultType;
    }

    @Override
    protected void onPostExecute(Integer resultType) {
        mProgressDialog.dismiss();
        switch (resultType){
            case AppUtils.CREATE_ADDRESS_OK:
                Toast.makeText(mContext, R.string.create_address_success, Toast.LENGTH_LONG).show();
                Intent intent = new Intent("com.x.wallet.action.BACKUP_MNEMONIC_ACTION");
                intent.putExtra(AppUtils.ADDRESS_URI, mUri);
                mContext.startActivity(intent);
                if(mContext instanceof Activity){
                    ((Activity) mContext).finish();
                }
                break;
            case AppUtils.CREATE_ADDRESS_FAILED_OTHER:
                Toast.makeText(mContext, R.string.create_address_failed, Toast.LENGTH_LONG).show();
                break;
            case AppUtils.CREATE_ADDRESS_FAILED_ACCOUNTNAME_SAME:
                Toast.makeText(mContext, R.string.create_address_failed_accountname_same, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
