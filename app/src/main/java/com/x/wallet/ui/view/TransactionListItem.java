package com.x.wallet.ui.view;

import android.content.Context;
import android.util.AttributeSet;
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
    private ImageView mTransactionStatus;
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
        mTransactionStatus = findViewById(R.id.transaction_status_coin);
        mTimeStamp = findViewById(R.id.time_stamp_tv);
        mAmount = findViewById(R.id.transaction_amount_tv);
        mCoinType = findViewById(R.id.coin_type_tv);

    }

    public void bind(TransactionItem item) {
        mTransactionItem = item;
        if (item == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(Long.parseLong(item.getTimeStamp()) * 1000L);
        String time = sdf.format(date);

        mTimeStamp.setText(time);

        if (item.getError()){
            mTransactionStatus.setImageResource(R.drawable.is_error);
        }else {
            if (item.getTxReceiptStatus() == 1){
                mTransactionStatus.setImageResource(R.drawable.is_ok);
            }
        }

        String amount = ExchangeCalUtil.getInstance().weiToEther(new BigInteger(item.getAmount())).stripTrailingZeros().toPlainString();
        if (item.getTransactionType().equalsIgnoreCase(TransactionItem.TRANSACTION_TYPE_RECEIVE)){
            mAmount.setText("+" + amount);
            mAmount.setTextColor(getResources().getColor(R.color.manage_account_textColor));
            mTransactionName.setText(getResources().getString(R.string.receipt_transaction) + ":  " + item.getFromAddress());
        }else {
            mAmount.setText("-" + amount);
            mAmount.setTextColor(getResources().getColor(R.color.colorRed));
            if (item.getToken()){
                mTransactionName.setText(getResources().getString(R.string.transfer_fax) + ":  " + item.getToAddress());
            }else {
                mTransactionName.setText(getResources().getString(R.string.send_out_transaction) + ":  " + item.getToAddress());
            }
        }

    }

    public TransactionItem getmTransactionItem() {
        return mTransactionItem;
    }
}
