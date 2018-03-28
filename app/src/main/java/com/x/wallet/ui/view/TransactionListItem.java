package com.x.wallet.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.lib.eth.util.ExchangeCalUtil;
import com.x.wallet.ui.data.TransactionItem;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wuliang on 18-3-16.
 */

public class TransactionListItem extends RelativeLayout{
    private TransactionItem mTransactionItem;
    private TextView mTransactionName;
    private ImageView mTransactionType;
    private TextView mTimeStamp;
    private TextView mAmount;
    private TextView mCoinType;

    public TransactionListItem(Context context) {
        super(context);
    }

    public TransactionListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransactionListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTransactionName = findViewById(R.id.transaction_name_tv);
        mTransactionType = findViewById(R.id.transaction_type_coin);
        mTimeStamp = findViewById(R.id.time_stamp_tv);
        mAmount = findViewById(R.id.transaction_amount_tv);
        mCoinType = findViewById(R.id.coin_type_tv);

    }

    public void bind(TransactionItem item) {
        mTransactionItem = item;

        mTransactionName.setText(item.getFromAddress() + item.getToAddress());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(Long.parseLong(item.getTimeStamp()) * 1000L);
        String time = sdf.format(date);

        mTimeStamp.setText(time);
        mAmount.setText(ExchangeCalUtil.getInstance().weiToEther(new BigInteger(item.getAmount())).toString());
        mCoinType.setText(item.getmCoinType());

        //have to check in/out
        mTransactionType.setImageResource(R.drawable.transaction_in);

    }

    public TransactionItem getmTransactionItem() {
        return mTransactionItem;
    }
}
