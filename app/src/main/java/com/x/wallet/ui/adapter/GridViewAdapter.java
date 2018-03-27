package com.x.wallet.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.ui.data.GridItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliang on 18-3-27.
 */

public class GridViewAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private int layoutResourceId;
    private List<String> mGridData = new ArrayList<String>();

    public GridViewAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.layoutResourceId = resource;
        this.mGridData = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.word_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String word = mGridData.get(position);
        holder.textView.setText(word);
        return convertView;
    }

    private class ViewHolder {
        TextView textView;
    }
}
