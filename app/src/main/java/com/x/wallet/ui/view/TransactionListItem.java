package com.x.wallet.ui.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.lib.eth.util.ExchangeCalUtil;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.ui.data.TransactionItem;

import java.math.BigDecimal;
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

    public void bind(Cursor cursor, String address, boolean isTokenAccount){
        mTransactionItem = TransactionItem.createFromCursor(cursor, address, isTokenAccount);
        bind(mTransactionItem, isTokenAccount);
    }

    public void bind(TransactionItem item, boolean isTokenAccount) {
        mTransactionItem = item;
        if (item == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(Long.parseLong(item.getTimeStamp()) * 1000L);
        String time = sdf.format(date);

        mTimeStamp.setText(time);

        if (item.getError() && !item.getToken()){
            mTransactionStatus.setImageResource(R.drawable.is_error);
        }else {
            if (item.getTxReceiptStatus() == 1){
                mTransactionStatus.setImageResource(R.drawable.is_ok);
            }
        }
        BigDecimal rawAmount = new BigDecimal(item.getAmount());
        String amount = rawAmount.divide(BigDecimal.TEN.pow(Integer.parseInt(item.getTokenDecimals()))).setScale(6,BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString();
        mCoinType.setText(item.getTokenSymbols());
        
        if (item.getTransactionType().equalsIgnoreCase(TransactionItem.TRANSACTION_TYPE_RECEIVE)){
            mAmount.setText("+" + amount);
            mAmount.setTextColor(getResources().getColor(R.color.manage_account_textColor));
            mTransactionName.setText(getResources().getString(R.string.receipt_transaction) + ":  " + item.getFromAddress());
        }else {
            mAmount.setText("-" + amount);
            mAmount.setTextColor(getResources().getColor(R.color.colorRed));
            if (isTokenAccount){
                mTransactionName.setText(getResources().getString(R.string.send_out_transaction) + ":  " + item.getToAddress());
                mCoinType.setText(item.getTokenSymbols());
            }else {
                if (item.getToken() || item.getError()) {
                    mTransactionName.setText(getResources().getString(R.string.transfer_fax) + ":  " + item.getToAddress());
                } else {
                    mTransactionName.setText(getResources().getString(R.string.send_out_transaction) + ":  " + item.getToAddress());
                }
            }
        }

    }

    public TransactionItem getmTransactionItem() {
        return mTransactionItem;
    }
}
