package com.x.wallet.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.R;
import com.x.wallet.ui.data.AddressItem;
import com.x.wallet.ui.view.AddressListItem;

public class FavoriteAddressAdapter extends CursorRecyclerAdapter<FavoriteAddressAdapter.ViewHolder> {
    private boolean isToChoose;
    private AddressItem item;
    private ItemClickListener mItemClickListener;

    public FavoriteAddressAdapter(Context context, Cursor c, int flags, boolean isToChoose) {
        super(context, c, flags);
        this.isToChoose = isToChoose;
        setHasStableIds(true);
    }

    @Override
    public void bindViewHolder(ViewHolder holder, Context context, Cursor cursor) {
        final AddressListItem listItem = (AddressListItem) holder.mView;
        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = listItem.getmItem();
                if (mItemClickListener != null){
                    mItemClickListener.onItemClick(listItem.getmItem());
                }
            }
        });

        listItem.bind(cursor);
    }

    @Override
    public ViewHolder createViewHolder(Context context, ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final AddressListItem addressListItem = (AddressListItem) inflater.inflate(R.layout.address_list_item, null);
        return new ViewHolder(addressListItem);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
    }

    public AddressItem getItem() {
        return item;
    }

    public void setItemClickListener(FavoriteAddressAdapter.ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void onItemClick(AddressItem item);
    }
}
