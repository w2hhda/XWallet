package com.x.wallet.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.x.wallet.R;

/**
 * Created by wuliang on 18-3-27.
 */

public class BackupMnemonicStepFirstView extends LinearLayout{

    private View mBackupRightnowBtn;

    public BackupMnemonicStepFirstView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.backup_mnemonic_step_first, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBackupRightnowBtn = findViewById(R.id.backup_rightnow_btn);
    }

    public void init(View.OnClickListener onClickListener){
        mBackupRightnowBtn.setOnClickListener(onClickListener);
    }
}
