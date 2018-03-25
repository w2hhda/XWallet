package com.x.wallet;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.x.wallet.ui.activity.BaseAppCompatActivity;
import com.x.wallet.ui.fragment.AccountListFragment;
import com.x.wallet.ui.fragment.SettingsFragment;
import com.x.wallet.ui.adapter.ViewPagerAdapter;

public class MainActivity extends BaseAppCompatActivity {

    private ViewPager mViewPager;
    private MenuItem mMenuItem;
    private BottomNavigationView mBottomNavigationView;
    private ViewPagerAdapter adapter;
    private Fragment[] fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews(){
        mViewPager = findViewById(R.id.viewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mMenuItem != null) {
                    mMenuItem.setChecked(false);
                } else {
                    mBottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                mMenuItem = mBottomNavigationView.getMenu().getItem(position);
                mMenuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setupViewPager();

        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_account:
                        mViewPager.setCurrentItem(0);
                        break;
                    case R.id.navigation_setting:
                        mViewPager.setCurrentItem(1);
                        break;
                }
                return true;
            }
        };

        mBottomNavigationView = findViewById(R.id.navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void setupViewPager() {
        fragments = new Fragment[2];
        fragments[0] = new AccountListFragment();
        fragments[1] = new SettingsFragment();

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(fragments[0]);
        adapter.addFragment(fragments[1]);
        //adapter.addFragment(new AccountListFragment());
        //adapter.addFragment(new SettingsFragment());
        mViewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
