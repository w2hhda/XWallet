package com.x.wallet.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.ui.data.TokenItemBean;
import com.x.wallet.ui.view.TokenListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliang on 18-3-30.
 */
public class AllSupportTokenListAdapter extends RecyclerView.Adapter<AllSupportTokenListAdapter.ViewHolder> {

    private int mListItemLayoutId;
    private ArrayList<TokenItemBean> mTokenItemList;
    private int mCurrentCheckedItemPosition;
    private ItemClickListener mItemClickListener;

    public AllSupportTokenListAdapter(int layoutId) {
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
                if(mItemClickListener != null){
                    mItemClickListener.onItemClick();
                }
            }
        });
        listItem.bind(mTokenItemList.get(listPosition), mCurrentCheckedItemPosition == listPosition);
    }

    public TokenItemBean getSelectedTokenItem() {
        return mCurrentCheckedItemPosition >= 0 ? mTokenItemList.get(mCurrentCheckedItemPosition) : null;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
    }

    public interface ItemClickListener{
        void onItemClick();
    }
}
