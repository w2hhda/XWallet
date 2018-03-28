package com.x.wallet.ui.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.x.wallet.R;
import com.x.wallet.ui.adapter.GridViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

/**
 * Created by wuliang on 18-3-27.
 */

public class BackupMnemonicStepThirdView extends LinearLayout{
    private Context mContext;
    private GridView mOutGridView;
    private GridView mInputGridView;

    private List<String> mInitialWords;
    private List<String> mShuffleWords;
    private LinkedHashSet<Integer> mCheckPositions;

    private View mLastBtn;

    public BackupMnemonicStepThirdView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(getContext()).inflate(R.layout.backup_mnemonic_step_third, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mOutGridView = findViewById(R.id.output_gridview);
        mInputGridView = findViewById(R.id.input_gridview);
        mInputGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        mInputGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(mCheckPositions.contains(position)){
                    mCheckPositions.remove(position);
                } else {
                    mCheckPositions.add(position);
                }
                updateOutGridView();
            }
        });

        mLastBtn = findViewById(R.id.confirm_mnemonic_btn);
        mLastBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mContext instanceof Activity){
                    if(mCheckPositions != null && mCheckPositions.size() == mInitialWords.size()
                            && isTheWordTheSame()){
                        Toast.makeText(mContext, R.string.backup_mnemonic_success, Toast.LENGTH_LONG).show();
                        ((Activity) mContext).finish();
                    } else {
                        Toast.makeText(mContext, R.string.backup_mnemonic_failed, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void updateOutGridView(){
        List<String> gridData = new ArrayList<>();
        for (Integer position : mCheckPositions) {
            gridData.add(mShuffleWords.get(position));
        }
        mOutGridView.setAdapter(new GridViewAdapter(mContext, R.layout.grid_item, gridData));
    }

    public void initWords(List<String> words) {
        mShuffleWords = words;
        mInitialWords = new ArrayList<>(words);
        Collections.shuffle(words, new Random(20));
        mInputGridView.setAdapter(new GridViewAdapter(mContext, R.layout.confirm_grid_item, words));
        mCheckPositions = new LinkedHashSet<>();
    }

    private boolean isTheWordTheSame(){
        int index = 0;
        for (Integer position : mCheckPositions) {
            if(!mInitialWords.get(index).equals(mShuffleWords.get(position))) return false;
            index++;
        }
        return true;
    }
}
