package com.x.wallet.ui.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.ui.data.AccountItem;

import net.bither.bitherj.core.BtcCreateAddressHelper;

/**
 * Created by wuliang on 18-3-16.
 */

public class ManageAccountActivity extends WithBackAppCompatActivity {
    private AccountItem mAccountItem;
    private TextView mAccountNameTv;
    private TextView mAddressTv;
    private TextView mBalanceTv;

    private View mMnemonicView;
    private View mKeyView;

    private View mDeleteView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_account_activity);

        mAccountItem = (AccountItem) getIntent().getSerializableExtra(AppUtils.ACCOUNT_DATA);

        initViews();
    }

    private void initViews(){
        mAddressTv = findViewById(R.id.address_tv);
        mAddressTv.setText(mAccountItem.getAddress());
        mAccountNameTv = findViewById(R.id.account_name_tv);
        mAccountNameTv.setText(mAccountItem.getAccountName());

        mBalanceTv = findViewById(R.id.balance_tv);

        mMnemonicView = findViewById(R.id.mnemonic_tv);
        mMnemonicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordDialog(AppUtils.IMPORTTYPE.IMPORT_TYPE_MNEMONIC);
            }
        });
        mKeyView = findViewById(R.id.key_tv);
        mKeyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordDialog(AppUtils.IMPORTTYPE.IMPORT_TYPE_KEY);
            }
        });

        mDeleteView = findViewById(R.id.delete_account_tv);
    }

    private void showPasswordDialog(final int importType){
        final LayoutInflater inflater = this.getLayoutInflater();
        final View contentView = inflater.inflate(R.layout.backup_account_dialog, null);
        final EditText passwordEt = contentView.findViewById(R.id.password_et);
        View passwordCheckBtn = contentView.findViewById(R.id.check_password_btn);
        final TextView resultTv = contentView.findViewById(R.id.result_tv);

        passwordCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(passwordEt.getText() == null || TextUtils.isEmpty(passwordEt.getText())){
                    return;
                }
                String result = importType == AppUtils.IMPORTTYPE.IMPORT_TYPE_MNEMONIC ?
                        BtcCreateAddressHelper.readMnemonic(mAccountItem.getEncryMnemonic(), passwordEt.getText().toString()):
                        BtcCreateAddressHelper.readPrivateKey(mAccountItem.getEncrySeed(), passwordEt.getText().toString());
                if(TextUtils.isEmpty(result)){
                    resultTv.setText(R.string.password_check_error);
                }else {
                    resultTv.setText(result);
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(importType == AppUtils.IMPORTTYPE.IMPORT_TYPE_MNEMONIC ?
                R.string.see_mnemonic : R.string.see_key);
        builder.setView(contentView);
        builder.setNegativeButton(R.string.finish, null);
        builder.show();
    }
}
