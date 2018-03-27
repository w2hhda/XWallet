package com.x.wallet.ui.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.x.wallet.R;
import com.x.wallet.ui.PasswordCheckDialogHelper;
import com.x.wallet.ui.adapter.GridViewAdapter;
import com.x.wallet.ui.data.GridItem;

import java.util.ArrayList;

/**
 * Created by wuliang on 18-3-27.
 */

public class BackupMnemonicActivity extends WithBackAppCompatActivity{

    private View mRemindBackupContainer;
    private View mBackupRightnowBtn;

    private View mShowMnemonicContainer;
    private Button mShowMnemonicBtn;
    private GridView mGridView;

    private View mConfirmMenmonicContainer;
    private GridView mOutGridView;
    private GridView mInputGridView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_mnemonic_activity);

        mRemindBackupContainer = findViewById(R.id.remind_backup_ll);
        mBackupRightnowBtn = findViewById(R.id.backup_rightnow_btn);

        mShowMnemonicContainer = findViewById(R.id.show_mnemonic_ll);
        mShowMnemonicBtn = findViewById(R.id.show_mnemonic_btn);
        mShowMnemonicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordCheckDialog();
            }
        });

        mBackupRightnowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRemindBackupContainer.setVisibility(View.GONE);
                mShowMnemonicContainer.setVisibility(View.VISIBLE);
            }
        });

        mGridView = findViewById(R.id.gridview);

        mConfirmMenmonicContainer = findViewById(R.id.backup_step_last_container);
        mOutGridView = findViewById(R.id.output_gridview);
        mInputGridView = findViewById(R.id.input_gridview);
    }

    private void showPasswordCheckDialog(){
        PasswordCheckDialogHelper.showPasswordDialog(this, new PasswordCheckDialogHelper.ConfirmBtnClickListener() {
            @Override
            public boolean onConfirmBtnClick(String password) {
                String result = "test test test";
                ArrayList<GridItem> gridData = new ArrayList<GridItem>();
                for (int i=0; i< 12; i++) {
                    GridItem item = new GridItem("test");
                    gridData.add(item);
                }
                mGridView.setAdapter(new GridViewAdapter(BackupMnemonicActivity.this, R.layout.grid_item, gridData));
                updateShowMnemonicBtn();
                return true;
            }
        });
    }

    private void updateShowMnemonicBtn(){
        mShowMnemonicBtn.setText(R.string.next_step);
        mShowMnemonicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowMnemonicContainer.setVisibility(View.GONE);
                mConfirmMenmonicContainer.setVisibility(View.VISIBLE);
                updateOutGridView();
                updateInputGridView();
            }
        });
    }

    private void updateOutGridView(){
        String result = "test test test";
        ArrayList<GridItem> gridData = new ArrayList<GridItem>();
        for (int i=0; i< 12; i++) {
            GridItem item = new GridItem("test");
            gridData.add(item);
        }
        mOutGridView.setAdapter(new GridViewAdapter(BackupMnemonicActivity.this, R.layout.grid_item, gridData));
    }

    private void updateInputGridView(){
        String result = "test test test";
        ArrayList<GridItem> gridData = new ArrayList<GridItem>();
        for (int i=0; i< 12; i++) {
            GridItem item = new GridItem("test");
            gridData.add(item);
        }
        mInputGridView.setAdapter(new GridViewAdapter(BackupMnemonicActivity.this, R.layout.confirm_grid_item, gridData));
    }
}
