package com.x.wallet.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;
import com.x.wallet.ui.view.CurrencyListItem;

/**
 * Created by wuliang on 18-3-30.
 */
public class CurrencyArrayAdapter extends RecyclerView.Adapter<CurrencyArrayAdapter.ViewHolder> {

    private int mListItemLayoutId;
    private String[] mCurrencyArray;
    private String[] mCurrencyUnitArray;
    private String mChooseCurrency;
    private String mChooseCurrencyUnit;

    private ItemClickListener mItemClickListener;

    public CurrencyArrayAdapter(int layoutId, String[] currencyArray, String[] currencyUnitArray) {
        mListItemLayoutId = layoutId;
        mCurrencyArray = currencyArray;
        mCurrencyUnitArray = currencyUnitArray;
        mChooseCurrency = UsdToCnyHelper.getChooseCurrency();
    }

    @Override
    public int getItemCount() {
        return mCurrencyArray == null ? 0 : mCurrencyArray.length;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mListItemLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int listPosition) {
        CurrencyListItem listItem = (CurrencyListItem) holder.mView;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastValue = mChooseCurrency;
                if(!lastValue.equals(mCurrencyArray[listPosition])){
                    mChooseCurrency = mCurrencyArray[listPosition];
                    mChooseCurrencyUnit = mCurrencyUnitArray[listPosition];
                    notifyDataSetChanged();
                }
                if(mItemClickListener != null){
                    mItemClickListener.onItemClick();
                }
            }
        });

        listItem.bind(mCurrencyArray[listPosition], mChooseCurrency);
    }

    public void saveCurrencyChoose(){
        UsdToCnyHelper.updateCurrentCheck(mChooseCurrency, mChooseCurrencyUnit);
    }

    public String getCurrentCurrency(){
        return mChooseCurrency;
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
