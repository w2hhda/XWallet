package com.x.wallet.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.x.wallet.R;

/**
 * Created by wuliang on 18-3-14.
 */

public class WithBackAppCompatActivity extends BaseAppCompatActivity{
    TextView titleTv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    private void setupActionBar(){
 //       ActionBar actionBar = getSupportActionBar();
//        if(actionBar != null){
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
        ActionBar bar =  getSupportActionBar();
        bar.setDisplayShowCustomEnabled(true);
        //bar.setHomeButtonEnabled(true);
        //bar.setDisplayHomeAsUpEnabled(true);
        View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.action_bar_layout, null);
        bar.setCustomView(v, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        titleTv = v.findViewById(R.id.title);
        ImageView imageView = v.findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        titleTv.setText(title);
    }
}
