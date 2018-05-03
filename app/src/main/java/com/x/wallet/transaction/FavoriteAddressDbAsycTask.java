package com.x.wallet.transaction;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.x.wallet.db.DbUtils;
import com.x.wallet.ui.data.AddressItem;

/**
 * Created by wuliang on 18-3-27.
 */

public class FavoriteAddressDbAsycTask extends AsyncTask<Void, Void, Boolean> {
    private long mOldId;
    private String mAddress;
    private String mType;
    private String mName;
    private int mActionType;
    private ProgressDialog mProgressDialog;

    private OnDataActionFinishedListener mOnDataActionFinishedListener;
    public static final int INSERT_ACTION = 1;
    public static final int DEL_ACTION = 2;
    public static final int UPDATE_ACTION = 3;

    public FavoriteAddressDbAsycTask(Context context, AddressItem item, int actionType,
                                     OnDataActionFinishedListener onDataActionFinishedListener) {
        mOldId = item.getId();
        mAddress = item.getAddress();
        mType = item.getAddressType();
        mName =item.getName();
        mActionType = actionType;
        mOnDataActionFinishedListener = onDataActionFinishedListener;
        mProgressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        switch (mActionType){
            case INSERT_ACTION:
                return DbUtils.insertFavoriteAddressIntoDb(mAddress, mType, mName);
            case DEL_ACTION:
                return DbUtils.deleteFavoriteAddress(mOldId);
            case UPDATE_ACTION:
                return DbUtils.updateFavoriteAddress(mOldId, mAddress, mType, mName);
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        mProgressDialog.dismiss();
        if(mOnDataActionFinishedListener != null){
            mOnDataActionFinishedListener.onDataActionFinished(isSuccess);
        }
    }

    public interface OnDataActionFinishedListener{
        void onDataActionFinished(boolean isSuccess);
    }
}
