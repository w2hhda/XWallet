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

/**
 * Created by wuliang on 18-3-15.
 */

public class CreateAddressAsycTask extends AsyncTask<Void, Void, Uri>{
    private int mCoinType;
    private String mPassword;
    private String mAccountName;

    private ProgressDialog mProgressDialog;
    private Context mContext;

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
    protected Uri doInBackground(Void... voids) {
        return AddressUtils.createAddress(mCoinType,
                mPassword,
                mAccountName);
    }

    @Override
    protected void onPostExecute(Uri uri) {
        mProgressDialog.dismiss();
        if(uri != null){
            Toast.makeText(mContext, R.string.create_address_success, Toast.LENGTH_LONG).show();
            Intent intent = new Intent("com.x.wallet.action.BACKUP_MNEMONIC_ACTION");
            intent.putExtra(AppUtils.ADDRESS_URI, uri);
            mContext.startActivity(intent);
        }else {
            Toast.makeText(mContext, R.string.create_address_failed, Toast.LENGTH_LONG).show();
        }
        if(mContext instanceof Activity){
            ((Activity) mContext).finish();
        }
    }
}
