package com.x.wallet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;

/**
 * Created by wuliang on 18-3-13.
 */

public class CoinTypeChooseActivity extends WithBackAppCompatActivity {
    private int mActionType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cointype_choose_activity);

        Intent intent = getIntent();
        mActionType = intent.getIntExtra(AppUtils.ACTION_TYPE, AppUtils.ACCOUNT_ACTION_TYPE_NEW);

        ListView listView = findViewById(R.id.listview);
        listView.setAdapter(new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1,
                this.getResources().getStringArray(R.array.support_coins_array)));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(mActionType == AppUtils.ACCOUNT_ACTION_TYPE_NEW){
                    Intent intent = new Intent("com.x.wallet.action.CREATE_ACCOUNT_ACTION");
                    intent.putExtra(AppUtils.COIN_TYPE, position);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent("com.x.wallet.action.IMPORT_ACCOUNT_ACTION");
                    intent.putExtra(AppUtils.COIN_TYPE, position);
                    startActivity(intent);
                }
            }
        });
    }


}
