package com.x.wallet.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.x.wallet.R;

/**
 * Created by wuliang on 18-3-27.
 */

public class PasswordCheckDialogHelper {
    private TextView mFirstTv;
    private Dialog mDialog;
    private Context mContext;

    public void showPasswordDialog(Activity activity, final ConfirmBtnClickListener confirmBtnClickListener, final int strId){
        mContext = activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View contentView = inflater.inflate(R.layout.password_confirm_dialog, null);
        builder.setView(contentView);
        mDialog = builder.create();
        mFirstTv = contentView.findViewById(R.id.tv);
        mFirstTv.setText(strId);
        final EditText passwordEt = contentView.findViewById(R.id.password_et);
        final View confirmBtn = contentView.findViewById(R.id.confirm_btn);
        View cancelBtn = contentView.findViewById(R.id.cancel_btn);
        passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count,
                                          final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before,
                                      final int count) {
                mFirstTv.setText(strId);
                confirmBtn.setEnabled(s != null && s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmBtnClickListener.onConfirmBtnClick(passwordEt.getText() != null ? passwordEt.getText().toString() : "", mContext);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog();
            }
        });
        mDialog.show();
    }

    public void updatePasswordCheckError(){
        mFirstTv.setText(R.string.check_password_error);
    }

    public void dismissDialog(){
        if(mDialog != null){
            mDialog.dismiss();
        }
    }

    public interface ConfirmBtnClickListener{
        boolean onConfirmBtnClick(String password, Context context);
    }
}
