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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.common.primitives.Longs;
import com.x.wallet.R;
import com.x.wallet.ui.view.TransactionListItem;

import net.bither.bitherj.core.Tx;

import java.util.List;


public class BtcTransactionListAdapter extends BaseAdapter{
    private Context mContext;
    private String address;
    private final List<Tx> mTransactions;
    protected LayoutInflater mInflater;
    private static final int mLayoutItemId = R.layout.transaction_list_item;

    public BtcTransactionListAdapter(Context context, String address, List transactions) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.address = address;
        mTransactions = transactions;
    }

    @Override
    public int getCount() {
        return mTransactions.size();
    }

    @Override
    public Object getItem(int position) {
        return mTransactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        Tx tx = (Tx) getItem(position);
        return Longs.fromByteArray(tx.getTxHash());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = mInflater.inflate(mLayoutItemId, parent, false);
        } else {
            view = convertView;
        }
        if(view instanceof TransactionListItem){
            TransactionListItem listItem = (TransactionListItem)view;
            listItem.bind((Tx) getItem(position), address);
        }
        return view;
    }
}
