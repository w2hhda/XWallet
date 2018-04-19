package com.x.wallet.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.ui.view.BackupMnemonicStepFirstView;
import com.x.wallet.ui.view.BackupMnemonicStepSecondView;
import com.x.wallet.ui.view.BackupMnemonicStepThirdView;

/**
 * Created by wuliang on 18-3-27.
 */

public class BackupMnemonicActivity extends WithBackAppCompatActivity{

    private BackupMnemonicStepFirstView mBackupMnemonicStepFirstView;
    private BackupMnemonicStepSecondView mBackupMnemonicStepSecondView;
    private BackupMnemonicStepThirdView mBackupMnemonicStepThirdView;

    private Uri mUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_mnemonic_activity);

        initData();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        mBackupMnemonicStepFirstView = findViewById(R.id.remind_backup_ll);
        mBackupMnemonicStepFirstView.init(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBackupMnemonicStepFirstView.setVisibility(View.GONE);
                mBackupMnemonicStepSecondView.setVisibility(View.VISIBLE);
            }
        });

        mBackupMnemonicStepSecondView = findViewById(R.id.show_mnemonic_ll);
        mBackupMnemonicStepSecondView.init(mUri, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBackupMnemonicStepSecondView.setVisibility(View.GONE);
                mBackupMnemonicStepThirdView.setVisibility(View.VISIBLE);
                mBackupMnemonicStepThirdView.initWords(mBackupMnemonicStepSecondView.getWords());
            }
        });

        mBackupMnemonicStepThirdView = findViewById(R.id.backup_step_last_container);
    }

    private void initData(){
        mUri = getIntent().getParcelableExtra(AppUtils.ADDRESS_URI);
    }
}
