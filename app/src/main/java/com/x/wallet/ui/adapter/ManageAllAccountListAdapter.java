package com.x.wallet.ui.adapter;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.ui.view.ManageAllAccountListItem;

/**
 * Created by wuliang on 18-3-16.
 */

public class ManageAllAccountListAdapter extends CursorRecyclerAdapter<ManageAllAccountListAdapter.AccountViewHolder> {

    private final View.OnClickListener mViewClickListener;

    public ManageAllAccountListAdapter(final Context context, final Cursor cursor,
                                       final View.OnClickListener viewClickListener) {
        super(context, cursor, 0);
        mViewClickListener = viewClickListener;
        setHasStableIds(true);
    }

    @Override
    public void bindViewHolder(AccountViewHolder holder, Context context, Cursor cursor) {
        final ManageAllAccountListItem accountListItem = (ManageAllAccountListItem) holder.mView;
        accountListItem.bind(cursor);
    }

    @Override
    public AccountViewHolder createViewHolder(Context context, ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final ManageAllAccountListItem messageListItem = (ManageAllAccountListItem)layoutInflater.inflate(R.layout.manage_all_account_list_item, parent, false);
        return new AccountViewHolder(messageListItem, mViewClickListener);
    }

    @Override
    public void onViewRecycled(AccountViewHolder holder) {
        super.onViewRecycled(holder);
        Log.i(AppUtils.APP_TAG, "onViewRecycled holder.mView = " + holder.mView);
    }

    @Override
    public void onViewDetachedFromWindow(AccountViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Log.i(AppUtils.APP_TAG, "onViewDetachedFromWindow holder.mView = " + holder.mView);
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        public AccountViewHolder(final View itemView, final View.OnClickListener viewClickListener) {
            super(itemView);
            mView = itemView;
            mView.setOnClickListener(viewClickListener);
        }
    }

}
