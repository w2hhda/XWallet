package com.x.wallet.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.x.wallet.R;
import com.x.wallet.transaction.usdtocny.ChangeCurrencyAsycTask;
import com.x.wallet.transaction.usdtocny.UsdToCnyHelper;
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
                this.getResources().getStringArray(R.array.support_currency_array),
                this.getResources().getStringArray(R.array.support_currency_unit_array),
                mCurrencyIcons);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mAdapter.setItemClickListener(new CurrencyArrayAdapter.ItemClickListener() {
            @Override
            public void onItemClick() {
                if(!(mAdapter.getCurrentCurrency().equals(UsdToCnyHelper.getChooseCurrency()))){
                    new ChangeCurrencyAsycTask(CurrencyActivity.this, mAdapter.getCurrentCurrency(), new ChangeCurrencyAsycTask.OnChangeFinishedListener() {
                        @Override
                        public void onChangeFinished(Double result) {
                            if(result > 0){
                                mAdapter.saveCurrencyChoose();
                                Intent intent = new Intent();
                                intent.putExtra(CHOOSE_CURRENCY, mAdapter.getCurrentCurrency());
                                setResult(Activity.RESULT_OK, intent);
                                CurrencyActivity.this.finish();
                                Toast.makeText(CurrencyActivity.this, R.string.change_currency_success, Toast.LENGTH_LONG).show();
                            }else {
                                Toast.makeText(CurrencyActivity.this, R.string.change_currency_failed, Toast.LENGTH_LONG).show();
                            }
                        }
                    }).execute();
                }
            }
        });
    }
    
    private final static int[] mCurrencyIcons = {
        R.drawable.currency_chf,
        R.drawable.currency_cny,
        R.drawable.currency_eur,
        R.drawable.currency_gbp,
        R.drawable.currency_hkd,
        R.drawable.currency_inr,
        R.drawable.currency_jpy,
        R.drawable.currency_krw,
        R.drawable.currency_nzd,
        R.drawable.currency_pln,
        R.drawable.currency_rub,
        R.drawable.currency_sgd,
        R.drawable.currency_thb,
        R.drawable.currency_usd
    };
}
