package com.x.wallet.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.lib.eth.util.ExchangeCalUtil;
import com.x.wallet.ui.data.TransactionItem;

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

        fromAddress.setText(item.getFromAddress());
        toAddress.setText(item.getToAddress());
        String amountResult = ExchangeCalUtil.getInstance().weiToEther(new BigInteger(item.getAmount())).stripTrailingZeros().toPlainString();
        amount.setText(amountResult + " ETH");

        String faxResult = ExchangeCalUtil.getInstance().weiToEther(new BigInteger(item.getTransactionFee())).stripTrailingZeros().toPlainString();
        fax.setText(faxResult + " ETH");
        hash.setText(item.getReceiptHash());
    }
}
