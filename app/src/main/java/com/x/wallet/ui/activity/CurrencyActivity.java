package com.x.wallet.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.x.wallet.R;
import com.x.wallet.ui.adapter.CurrencyArrayAdapter;

/**
 * Created by admin on 2018/4/7.
 */

public class CurrencyActivity extends WithBackAppCompatActivity{
    private RecyclerView mRecyclerView;
    private CurrencyArrayAdapter mAdapter;
    public static final String CHOOSE_CURRENCY = "choose_currency";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.currency_activity);

        initRecyclerView();
    }

    private void initRecyclerView(){
        mRecyclerView = findViewById(R.id.recyclerView);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new CurrencyArrayAdapter(R.layout.currency_list_item,
                this.getResources().getStringArray(R.array.support_currency_array));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mAdapter.setItemClickListener(new CurrencyArrayAdapter.ItemClickListener() {
            @Override
            public void onItemClick() {
                Intent intent = new Intent();
                intent.putExtra(CHOOSE_CURRENCY, mAdapter.getCurrentCurrency());
                setResult(Activity.RESULT_OK, intent);
                CurrencyActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.saveCurrencyChoose();
    }
}
