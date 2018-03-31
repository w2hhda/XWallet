package com.x.wallet.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.R;
import com.x.wallet.ui.data.TransactionItem;
import com.x.wallet.ui.view.TransactionListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 27/3/2018.
 */

public class AccountDetailAdapter extends RecyclerView.Adapter<AccountDetailAdapter.ViewHolder> {

    private final View.OnClickListener mViewClickListener;
    private Context mContext;
    private List<TransactionItem> items;

    public AccountDetailAdapter(Context context,  View.OnClickListener mViewClickListener) {
        this.mContext = context;
        this.mViewClickListener = mViewClickListener;
        setHasStableIds(true);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final TransactionListItem transactionListItem = (TransactionListItem) holder.view;
        TransactionItem item = items.get(position);
        transactionListItem.bind(item);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        final TransactionListItem messageListItem = (TransactionListItem) layoutInflater.inflate(R.layout.transaction_list_item, null);
        return new AccountDetailAdapter.ViewHolder(messageListItem, mViewClickListener);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    public void addItems(List<TransactionItem> items){
        this.items = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        public ViewHolder(View itemView, final View.OnClickListener viewClickListener) {
            super(itemView);
            view = itemView;
            view.setOnClickListener(viewClickListener);
        }
    }
}
