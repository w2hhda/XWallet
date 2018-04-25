package com.x.wallet.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.R;
import com.x.wallet.ui.view.AccountTokenListItem;

public class AccountTokenListAdapter extends CursorRecyclerAdapter<AccountTokenListAdapter.ViewHolder> {
    private final View.OnClickListener mViewClickListener;

    public AccountTokenListAdapter(Context context, Cursor c, View.OnClickListener clickListener) {
        super(context, c, 0);
        mViewClickListener = clickListener;
        setHasStableIds(true);
    }

    @Override
    public void bindViewHolder(ViewHolder holder, Context context, Cursor cursor) {
        AccountTokenListItem listItem = (AccountTokenListItem) holder.mView;
        listItem.bind(cursor);
    }

    @Override
    public ViewHolder createViewHolder(Context context, ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_token_list_item, parent, false);
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
            mView.findViewById(R.id.delete_iv).setOnClickListener(listener);
        }
    }
}
