package com.x.wallet.ui.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.ui.data.AccountItem;

/**
 * Created by wuliang on 18-3-16.
 */

public class AccountDetailActivity extends WithBackAppCompatActivity {
    private AccountItem mAccountItem;

    private TextView mBalanceTranslateTv;
    private TextView mBalanceTv;
    private View mSendOutBtn;
    private View mReceiptBtn;

    public final static String SHARE_ADDRESS_EXTRA = "share_address_extra";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_detail_activity);

        mAccountItem = (AccountItem) getIntent().getSerializableExtra(AppUtils.ACCOUNT_DATA);
        this.setTitle(mAccountItem.getAccountName());

        initViews();
    }

    private void initViews(){
        mBalanceTranslateTv = findViewById(R.id.balance_translate_tv);

        mBalanceTv = findViewById(R.id.balance_tv);
        mSendOutBtn = findViewById(R.id.send_btn);
        mReceiptBtn = findViewById(R.id.receipt_btn);

        mReceiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.x.wallet.action_SHARE_ADDRESS_QR_ACTION");
                intent.putExtra(SHARE_ADDRESS_EXTRA,mAccountItem.getAddress());
                startActivity(intent);
            }
        });
    }
}
