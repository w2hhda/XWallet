package com.x.wallet.ui.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.ui.data.AddressItem;

public class AddressListItem extends RelativeLayout {
    private TextView nameView;
    private TextView addressView;
    private TextView addressTypeView;
    private AddressItem mItem;

    public AddressListItem(Context context) {
        super(context);
    }

    public AddressListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AddressListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        nameView = findViewById(R.id.address_name);
        addressView = findViewById(R.id.address_detail_tv);
        addressTypeView = findViewById(R.id.address_type);
    }

    public void bind(Cursor cursor){
        mItem = AddressItem.createFromCursor(cursor);
        nameView.setText(mItem.getName());
        addressTypeView.setText(mItem.getAddressType());
        addressView.setText(mItem.getAddress());
    }

    public AddressItem getmItem() {
        return mItem;
    }
}
