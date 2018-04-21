package com.x.wallet.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.btc.BtcUtils;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.ui.data.TransactionItem;

import java.math.BigDecimal;

/**
 * Created by Nick on 28/3/2018.
 */

public class TransactionDetailActivity extends WithBackAppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_detail_activity);
        setTitle(getResources().getString(R.string.transaction_detail));
        initView();
    }

    private void initView(){
        TextView fromAddressTv = findViewById(R.id.transaction_detail_from);
        TextView toAddressTv   = findViewById(R.id.transaction_detail_to);
        TextView amountTv      = findViewById(R.id.transaction_detail_amount);
        TextView feeTv         = findViewById(R.id.transaction_detail_fax);
        TextView hashTv        = findViewById(R.id.transaction_detail_hash);
        TransactionItem item = (TransactionItem) getIntent().getSerializableExtra(AppUtils.TRANSACTION_ITEM);
        boolean isTokenAccount = getIntent().getBooleanExtra(AppUtils.ACCOUNT_TYPE, false);

        fromAddressTv.setText(item.getFromAddress());
        toAddressTv.setText(item.getToAddress());

        if(isBtcAccount()){
            bindAmount(item, amountTv, feeTv);
        } else {
            bindAmount(item, isTokenAccount, amountTv, feeTv);
        }

        hashTv.setText(item.getReceiptHash());
    }

    private void bindAmount(TransactionItem item, boolean isTokenAccount, TextView amountTv, TextView feeTv){
        BigDecimal rawAmount = new BigDecimal(item.getAmount());
        int decimal = 18;
        if (isTokenAccount){
            decimal = Integer.parseInt(item.getTokenDecimals());
        }else if (item.getToken() || item.getError()){
            decimal = 18;
        }

        String amountResult = rawAmount.divide(BigDecimal.TEN.pow(decimal)).setScale(6,BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString();
        //String amountResult = ExchangeCalUtil.getInstance().weiToEther(new BigDecimal(item.getAmount())).stripTrailingZeros().toPlainString();
        String symbols = "ETH";

        if (isTokenAccount){
            symbols = item.getTokenSymbols();
        }else if (item.getError() || item.getToken()){
            if (item.getTokenSymbols() != null) {
                symbols = item.getTokenSymbols();
            }
        }else {
            symbols = item.getmCoinType();
        }

        String amountString = getPrefix(item.getTransactionType()) + amountResult + " " + symbols;
        amountTv.setText(amountString);

        feeTv.setText(TokenUtils.getBalanceText(item.getTransactionFee(), TokenUtils.ETH_DECIMALS) + " ETH");
    }

    private void bindAmount(TransactionItem item, TextView amountTv, TextView feeTv){
        String prefix = getPrefix(item.getTransactionType());
        amountTv.setText(prefix + TokenUtils.getBalanceText(item.getAmount(), BtcUtils.BTC_DECIMALS_COUNT) + " BTC");
        feeTv.setText(TokenUtils.getBalanceText(item.getTransactionFee(), BtcUtils.BTC_DECIMALS_COUNT) + " BTC");
    }

    private String getPrefix(String transactionType){
        return transactionType.equals(TransactionItem.TRANSACTION_TYPE_RECEIVE) ? "+ " : "- ";
    }

    private boolean isBtcAccount(){
        return getIntent().getIntExtra(AppUtils.COIN_TYPE, -1) == LibUtils.COINTYPE.COIN_BTC;
    }
}
