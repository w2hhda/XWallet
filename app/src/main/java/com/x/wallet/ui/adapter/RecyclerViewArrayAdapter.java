package com.x.wallet.ui.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.ui.data.TokenItemBean;
import com.x.wallet.ui.view.TokenListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliang on 18-3-30.
 */
public class RecyclerViewArrayAdapter extends RecyclerView.Adapter<RecyclerViewArrayAdapter.ViewHolder> {

    private int mListItemLayoutId;
    private ArrayList<TokenItemBean> mTokenItemList;
    private int mCurrentCheckedItemPosition;
    private ItemClickListener mItemClickListener;
    private String address;

    public RecyclerViewArrayAdapter(int layoutId, String accountAddress) {
        mListItemLayoutId = layoutId;
        mTokenItemList = new ArrayList<>();
        mCurrentCheckedItemPosition = -1;
        address = accountAddress;
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
        String selection = DbUtils.TokenTableColumns.ACCOUNT_ADDRESS + " = ?";
        Cursor cursor = XWalletApplication.getApplication().getContentResolver().query(XWalletProvider.CONTENT_URI_TOKEN,
                new String[]{DbUtils.TokenTableColumns.NAME,},selection, new String[]{address}, null);
        StringBuilder tokens = new StringBuilder();
        while (cursor.moveToNext()){
            String tokenName = cursor.getString(0);
            if (tokenName == null){
                continue;
            }
            tokens.append(tokenName);
            tokens.append(",");
        }
        String namePosition = mTokenItemList.get(listPosition).getName();
        if (tokens.toString().contains(namePosition)){
            listItem.bind(mTokenItemList.get(listPosition), true);
        }else {
            listItem.bind(mTokenItemList.get(listPosition), mCurrentCheckedItemPosition == listPosition);
        }
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
