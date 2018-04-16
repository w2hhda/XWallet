package com.x.wallet.ui.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.btc.BtcUtils;
import com.x.wallet.lib.btc.BtcLibHelper;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.ui.data.TransactionItem;

import net.bither.bitherj.core.Tx;
import net.bither.bitherj.exception.ScriptException;
import net.bither.bitherj.utils.Utils;

import java.math.BigDecimal;

/**
 * Created by wuliang on 18-3-16.
 */

public class TransactionListItem extends RelativeLayout{
    private TransactionItem mTransactionItem;
    private TextView mTransactionName;
    private ImageView mTransactionStatus;
    private TextView mTimeStamp;
    private TextView mAmount;
    private TextView mCoinUnitTv;

    private Tx mTransaction;
    private String mAddress;

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
        mCoinUnitTv = findViewById(R.id.coin_unit_tv);
    }

    public void bind(Cursor cursor, String address, boolean isTokenAccount){
        mTransactionItem = TransactionItem.createFromCursor(cursor, address, isTokenAccount);
        bind(mTransactionItem, isTokenAccount);
    }

    private void bind(TransactionItem item, boolean isTokenAccount) {
        if (item == null) return;
        mTransactionItem = item;

        mTimeStamp.setText(AppUtils.formatDate(Long.parseLong(item.getTimeStamp())));

        if (item.getError() && !item.getToken()){
            mTransactionStatus.setImageResource(R.drawable.is_error);
        }else {
            if (item.getTxReceiptStatus() == 1){
                mTransactionStatus.setImageResource(R.drawable.is_ok);
            }
        }
        BigDecimal rawAmount = new BigDecimal(item.getAmount());
        String amount = rawAmount.divide(BigDecimal.TEN.pow(Integer.parseInt(item.getTokenDecimals()))).setScale(6,BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString();
        mCoinUnitTv.setText(item.getTokenSymbols());
        
        if (item.getTransactionType().equalsIgnoreCase(TransactionItem.TRANSACTION_TYPE_RECEIVE)){
            mAmount.setText(getResources().getString(R.string.receive_amount_prefix, amount));
            mAmount.setTextColor(getResources().getColor(R.color.manage_account_textColor));
            mTransactionName.setText(getResources().getString(R.string.receipt_transaction_prefix, item.getFromAddress()));
        }else {
            mAmount.setText(getResources().getString(R.string.send_amount_prefix, amount));
            mAmount.setTextColor(getResources().getColor(R.color.colorRed));
            if (isTokenAccount){
                mTransactionName.setText(getResources().getString(R.string.send_out_transaction_prefix, item.getToAddress()));
                mCoinUnitTv.setText(item.getTokenSymbols());
            }else {
                if (item.getToken() || item.getError()) {
                    mTransactionName.setText(getResources().getString(R.string.transfer_fee, item.getToAddress()));
                } else {
                    mTransactionName.setText(getResources().getString(R.string.send_out_transaction_prefix, item.getToAddress()));
                }
            }
        }
    }

    public TransactionItem getTransactionItem() {
        return mTransactionItem;
    }

    public void bind(Tx transaction, String address) {
        this.mTransaction = transaction;
        this.mAddress = address;

        mCoinUnitTv.setText(R.string.coin_unit_btc);
        bindDate();
        long value = bindAmount();
        boolean isReceived = value >= 0;
        bindAddress(isReceived);
    }

    private void bindDate(){
        int time = mTransaction.getTxTime();
        if (time == 0) {
            mTimeStamp.setText("");
        } else {
            mTimeStamp.setText(AppUtils.formatDate(time));
        }
    }

    private long bindAmount(){
        long value;
        try {
            value = BtcLibHelper.deltaAmountFrom(mAddress, mTransaction);
        } catch (Exception e) {
            return 0;
        }
        if(value > 0){
            mAmount.setText(getResources().getString(R.string.receive_amount_prefix, TokenUtils.getBalanceText(value, BtcUtils.BTC_DECIMALS_COUNT)));
            mAmount.setTextColor(getResources().getColor(R.color.manage_account_textColor));
        } else {
            mAmount.setText(getResources().getString(R.string.send_amount_prefix, TokenUtils.getBalanceText(Math.abs(value), BtcUtils.BTC_DECIMALS_COUNT)));
            mAmount.setTextColor(getResources().getColor(R.color.colorRed));
        }
        return value;
    }

    private void bindAddress(boolean isReceived){
        if (isReceived) {
            if (mTransaction.isCoinBase()) {
                mTransactionName.setText("Mining");
            } else {
                try {
                    String subAddress = mTransaction.getFromAddress();
                    if (Utils.isEmpty(subAddress)) {
                        mTransactionName.setText(getResources().getString(R.string.receipt_transaction_prefix, "---"));
                    } else {
                        mTransactionName.setText(Utils.shortenAddress(subAddress));
                    }
                } catch (ScriptException e) {
                    e.printStackTrace();
                    mTransactionName.setText(getResources().getString(R.string.receipt_transaction_prefix, "---"));
                }
            }
        } else {
            String subAddress = mTransaction.getFirstOutAddress();
            if (subAddress != null) {
                mTransactionName.setText(getResources().getString(R.string.send_out_transaction_prefix, subAddress));
            } else {
                mTransactionName.setText(getResources().getString(R.string.send_out_transaction_prefix, "---"));
            }
        }
    }
}
