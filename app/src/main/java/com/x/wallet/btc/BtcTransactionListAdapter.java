/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.x.wallet.btc;

import android.support.annotation.NonNull;
import android.support.v7.util.AsyncListUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.R;
import com.x.wallet.ui.view.TransactionListItem;

import net.bither.bitherj.core.Tx;


public class BtcTransactionListAdapter extends RecyclerView.Adapter<BtcTransactionListAdapter.TransactionListItemViewHolder>{
    private String mAddress;
    private AsyncListUtil<Tx> mAsyncListUtil;

    public BtcTransactionListAdapter(String address, AsyncListUtil<Tx> asyncListUtil) {
        mAddress = address;
        mAsyncListUtil = asyncListUtil;
    }

    @NonNull
    @Override
    public TransactionListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list_item, parent, false);
        return new TransactionListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionListItemViewHolder holder, int position) {
        TransactionListItem listItem = (TransactionListItem) holder.mView;
        Tx tx = mAsyncListUtil.getItem(position);
        if(tx != null){
            listItem.bind(tx, mAddress);
        }
    }

    @Override
    public int getItemCount() {
        return mAsyncListUtil.getItemCount();
    }

    public static class TransactionListItemViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        public TransactionListItemViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
        }
    }
}
