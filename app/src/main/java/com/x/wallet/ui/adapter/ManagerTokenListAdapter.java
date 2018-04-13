package com.x.wallet.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.transaction.token.DeleteTokenAsyncTask;
import com.x.wallet.ui.activity.ManageAccountActivity;
import com.x.wallet.ui.data.TokenItem;
import com.x.wallet.ui.dialog.ContentShowDialogHelper;
import com.x.wallet.ui.view.ManagerTokenListItem;

import java.util.ArrayList;
import java.util.List;

public class ManagerTokenListAdapter extends CursorRecyclerAdapter<ManagerTokenListAdapter.ViewHolder> {
    private int layoutId;
    private final View.OnClickListener mViewClickListener;

    public ManagerTokenListAdapter(Context context, Cursor c, int layoutId, View.OnClickListener clickListener) {
        super(context, c, 0);
        this.layoutId = layoutId;
        mViewClickListener = clickListener;
        setHasStableIds(true);
    }

    @Override
    public void bindViewHolder(ViewHolder holder, Context context, Cursor cursor) {
        ManagerTokenListItem listItem = (ManagerTokenListItem) holder.mView;
        listItem.bind(cursor);
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
            mView.findViewById(R.id.delete_iv).setOnClickListener(listener);
        }
    }
}
