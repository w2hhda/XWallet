package com.x.wallet.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.x.wallet.R;
import com.x.wallet.transaction.token.DeleteTokenAsyncTask;
import com.x.wallet.ui.data.TokenItem;
import com.x.wallet.ui.dialog.ContentShowDialogHelper;
import com.x.wallet.ui.view.ManagerTokenListItem;

import java.util.ArrayList;
import java.util.List;

public class ManagerTokenListAdapter extends RecyclerView.Adapter<ManagerTokenListAdapter.ViewHolder> {
    private List<TokenItem> tokenItems;
    private int layoutId;
    private String address;
    private Handler handler;
    public ManagerTokenListAdapter(int layoutId, String address, Handler handler) {
        this.layoutId = layoutId;
        this.address = address;
        tokenItems = new ArrayList<>();
        this.handler = handler;
    }

    public void addAll(List<TokenItem> items){
        tokenItems.addAll(items);
        notifyDataSetChanged();

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position ) {
        ManagerTokenListItem listItem = (ManagerTokenListItem) holder.mView;
        final String accountName = tokenItems.get(position).getName();
        final Context context = holder.mView.getContext();
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentShowDialogHelper.showConfirmDialog(context, R.string.delete_token
                        , context.getResources().getString(R.string.confirm_delete_token, accountName)
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                new DeleteTokenAsyncTask(context,
                                        tokenItems.get(position),
                                        address,
                                        new DeleteTokenAsyncTask.OnDeleteTokenFinishedListener() {
                                            @Override
                                            public void onDeleteFinished() {
                                                tokenItems.remove(tokenItems.get(position));
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, tokenItems.size());
                                                //notifyDataSetChanged();
                                                handler.sendEmptyMessage(-1);
                                                Toast.makeText(context, "delete ok!", Toast.LENGTH_SHORT).show();
                                            }
                                        }).execute();
                            }
                        });
            }
        });
        listItem.bind(tokenItems.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return tokenItems == null ? 0 : tokenItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            imageView = mView.findViewById(R.id.delete_iv);
        }
    }
}
