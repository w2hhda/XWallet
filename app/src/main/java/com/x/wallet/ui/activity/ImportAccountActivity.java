package com.x.wallet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.ui.TabUtils;
import com.x.wallet.ui.adapter.ViewPagerAdapter;
import com.x.wallet.ui.fragment.ImportKeyFragment;
import com.x.wallet.ui.fragment.ImportKeyStoreFragment;
import com.x.wallet.ui.fragment.ImportMnemonicFragment;

/**
 * Created by wuliang on 18-3-16.
 */

public class ImportAccountActivity extends WithBackAppCompatActivity {
    private int mCoinType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_account_activity);
        initData();
        initViews();
    }

    private void initViews() {
        ViewPager pager = findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        bundle.putInt(AppUtils.COIN_TYPE, mCoinType);

        ImportMnemonicFragment mnemonicFragment = new ImportMnemonicFragment();
        mnemonicFragment.setArguments(bundle);
        adapter.addFragment(mnemonicFragment, this.getResources().getText(R.string.mnemonic).toString());

        ImportKeyFragment keyFragment = new ImportKeyFragment();
        keyFragment.setArguments(bundle);
        adapter.addFragment(keyFragment, this.getResources().getText(R.string.key).toString());

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(this.getResources().getText(R.string.mnemonic)));
        tabLayout.addTab(tabLayout.newTab().setText(this.getResources().getText(R.string.key)));

        if(mCoinType == LibUtils.COINTYPE.COIN_ETH){
            ImportKeyStoreFragment keyStoreFragment = new ImportKeyStoreFragment();
            keyStoreFragment.setArguments(bundle);
            adapter.addFragment(keyStoreFragment, this.getResources().getText(R.string.keystore).toString());
            tabLayout.addTab(tabLayout.newTab().setText(this.getResources().getText(R.string.keystore)));
        }

        pager.setAdapter(adapter);

        tabLayout.setupWithViewPager(pager);
        final int margin = this.getResources().getDimensionPixelSize(R.dimen.tab_margin);
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                TabUtils.updateTabMargin(tabLayout, margin, margin);
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        mCoinType = intent.getIntExtra(AppUtils.COIN_TYPE, LibUtils.COINTYPE.COIN_BTC);
    }
}
