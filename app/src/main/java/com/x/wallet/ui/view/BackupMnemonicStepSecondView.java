package com.x.wallet.ui.view;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.x.wallet.R;
import com.x.wallet.transaction.mnemonic.DecryptMnemonicAsycTask;
import com.x.wallet.ui.dialog.PasswordCheckDialogHelper;
import com.x.wallet.ui.adapter.GridViewAdapter;

import java.util.List;

/**
 * Created by wuliang on 18-3-27.
 */

public class BackupMnemonicStepSecondView extends LinearLayout {
    private Uri mUri;
    private Context mContext;
    private Button mShowMnemonicBtn;
    private GridView mGridView;

    private View.OnClickListener mShowMnemonicBtnClickListener;
    private List<String> mWords;

    public BackupMnemonicStepSecondView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(getContext()).inflate(R.layout.backup_mnemonic_step_second, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mShowMnemonicBtn = findViewById(R.id.show_mnemonic_btn);
        mShowMnemonicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordCheckDialog();
            }
        });
        mGridView = findViewById(R.id.gridview);
    }

    public void init(Uri uri, View.OnClickListener showMnemonicBtnClickListener){
        mUri = uri;
        mShowMnemonicBtnClickListener = showMnemonicBtnClickListener;
    }

    private void showPasswordCheckDialog(){
        final PasswordCheckDialogHelper mPasswordCheckDialogHelper = new PasswordCheckDialogHelper();

        mPasswordCheckDialogHelper.showPasswordDialog((Activity)mContext, new PasswordCheckDialogHelper.ConfirmBtnClickListener() {
            @Override
            public void onConfirmBtnClick(String password, Context context) {
                new DecryptMnemonicAsycTask(mContext, mUri, password, new DecryptMnemonicAsycTask.OnDecryptMnemonicFinishedListener() {
                    @Override
                    public void onDecryptMnemonicFinished(List<String> words) {
                        mWords = words;
                        if(words != null && words.size() > 0){
                            mPasswordCheckDialogHelper.dismissDialog();
                            mGridView.setAdapter(new GridViewAdapter(mContext, R.layout.grid_item, words));
                            updateShowMnemonicBtn();
                        } else {
                            mPasswordCheckDialogHelper.updatePasswordCheckError();
                        }
                    }
                }).execute();
            }
        }, R.string.confirm_password_to_get_mnemonic);
    }

    private void updateShowMnemonicBtn(){
        mShowMnemonicBtn.setText(R.string.next_step);
        mShowMnemonicBtn.setOnClickListener(mShowMnemonicBtnClickListener);
    }

    public List<String> getWords() {
        return mWords;
    }
}
