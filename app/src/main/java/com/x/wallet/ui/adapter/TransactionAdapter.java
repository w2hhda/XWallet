package com.x.wallet.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.ui.view.TransactionListItem;

public class TransactionAdapter extends CursorRecyclerAdapter<TransactionAdapter.ViewHolder> {
    private int layoutId;
    private String address;
    private boolean isTokenAccount;
    private final View.OnClickListener mViewClickListener;

    public TransactionAdapter(Context context, Cursor c, int layoutId, String address, boolean isTokenAccount, View.OnClickListener clickListener) {
        super(context, c, 0);
        this.layoutId = layoutId;
        this.address = address;
        this.isTokenAccount = isTokenAccount;
        mViewClickListener = clickListener;
        setHasStableIds(true);
    }

    @Override
    public void bindViewHolder(ViewHolder holder, Context context, Cursor cursor) {
        TransactionListItem listItem = (TransactionListItem) holder.mView;
        listItem.bind(cursor, address, isTokenAccount);
    }

    @Override
    public ViewHolder createViewHolder(Context context, ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view, mViewClickListener);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public ViewHolder(View itemView, View.OnClickListener listener) {
            super(itemView);
            mView = itemView;
            mView.setOnClickListener(listener);
        }
    }
}
