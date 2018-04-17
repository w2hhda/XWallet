package com.x.wallet.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.lib.eth.util.ExchangeCalUtil;
import com.x.wallet.ui.data.TransactionItem;

import java.math.BigDecimal;
import java.math.BigInteger;

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
        TextView fromAddress = findViewById(R.id.transaction_detail_from);
        TextView toAddress   = findViewById(R.id.transaction_detail_to);
        TextView amount      = findViewById(R.id.transaction_detail_amount);
        TextView fax         = findViewById(R.id.transaction_detail_fax);
        TextView hash        = findViewById(R.id.transaction_detail_hash);
        TransactionItem item = (TransactionItem) getIntent().getSerializableExtra(AppUtils.TRANSACTION_ITEM);
        boolean isTokenAccount = getIntent().getBooleanExtra(AppUtils.ACCOUNT_TYPE, false);

        fromAddress.setText(item.getFromAddress());
        toAddress.setText(item.getToAddress());
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

        boolean isReceive = false;
        if (item.getTransactionType().equals(TransactionItem.TRANSACTION_TYPE_RECEIVE)){
            isReceive = true;
        }
        String amountString = amountResult + " " + symbols;
        if (isReceive){
            amountString = "+ " + amountString;
        }else {
            amountString = "-" + amountString;
        }

        amount.setText(amountString);

        String faxResult = ExchangeCalUtil.getInstance().weiToEther(new BigInteger(item.getTransactionFee())).stripTrailingZeros().toPlainString();
        fax.setText(faxResult + " ETH");
        hash.setText(item.getReceiptHash());
    }
}
