package com.x.wallet.ui.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private TextView mAddressTv;
    private TextView mBalanceTv;
    private View mSendOutBtn;
    private View mReceiptBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_detail_activity);

        mAccountItem = (AccountItem) getIntent().getSerializableExtra(AppUtils.ACCOUNT_DATA);
        this.setTitle(mAccountItem.getAccountName());

        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manage_account_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_manage_account){
            Intent intent = new Intent("com.x.wallet.action.MANAGE_ACCOUNT_ACTION");
            intent.putExtra(AppUtils.ACCOUNT_DATA, mAccountItem);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews(){
        mAddressTv = findViewById(R.id.address_tv);
        mAddressTv.setText(mAccountItem.getAddress());

        mBalanceTv = findViewById(R.id.balance_tv);
        mSendOutBtn = findViewById(R.id.send_btn);
        mReceiptBtn = findViewById(R.id.receipt_btn);
    }
}
