package com.x.wallet.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.ui.data.TokenItem;
import com.x.wallet.ui.view.TokenListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliang on 18-3-30.
 */
public class RecyclerViewArrayAdapter extends RecyclerView.Adapter<RecyclerViewArrayAdapter.ViewHolder> {

    private int mListItemLayoutId;
    private ArrayList<TokenItem> mTokenItemList;
    private int mCurrentCheckedItemPosition;

    public RecyclerViewArrayAdapter(int layoutId) {
        mListItemLayoutId = layoutId;
        mTokenItemList = new ArrayList<>();
        mCurrentCheckedItemPosition = -1;
    }

    public void addAll(List list){
        mTokenItemList.addAll(list);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTokenItemList == null ? 0 : mTokenItemList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mListItemLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int listPosition) {
        TokenListItem listItem = (TokenListItem) holder.mView;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int lastPosition = mCurrentCheckedItemPosition;
                if(lastPosition != listPosition){
                    mCurrentCheckedItemPosition = listPosition;
                    notifyDataSetChanged();
                }
            }
        });

        listItem.bind(mTokenItemList.get(listPosition), mCurrentCheckedItemPosition == listPosition);
    }

    public TokenItem getSelectedTokenItem() {
        return mTokenItemList.get(mCurrentCheckedItemPosition);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
    }
}
