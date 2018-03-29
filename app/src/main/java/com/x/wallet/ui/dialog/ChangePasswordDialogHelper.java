package com.x.wallet.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.ui.view.SetPasswordView;

/**
 * Created by wuliang on 18-3-27.
 */

public class ChangePasswordDialogHelper {
    private TextView mFirstTv;
    private Dialog mDialog;
    private Context mContext;

    public void showPasswordDialog(Activity activity, final ConfirmBtnClickListener confirmBtnClickListener, final int strId) {
        mContext = activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View contentView = inflater.inflate(R.layout.change_password_dialog, null);
        builder.setView(contentView);
        mDialog = builder.create();
        mFirstTv = contentView.findViewById(R.id.tv);
        updateFirstTv(strId, R.color.manage_account_textColor);
        final EditText currentPasswordEt = contentView.findViewById(R.id.current_password_et);
        final EditText newPasswordEt = contentView.findViewById(R.id.new_password_et);
        final EditText confirmPasswordEt = contentView.findViewById(R.id.confirm_new_password_et);
        final View confirmBtn = contentView.findViewById(R.id.confirm_btn);
        View cancelBtn = contentView.findViewById(R.id.cancel_btn);
        currentPasswordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count,
                                          final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before,
                                      final int count) {
                updateFirstTv(strId, R.color.manage_account_textColor);
                confirmBtn.setEnabled(s != null && s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPasswordEt.getText() == null || TextUtils.isEmpty(currentPasswordEt.getText().toString())) {
                    Toast.makeText(mContext, R.string.password_error_blank, Toast.LENGTH_LONG).show();
                    return;
                }

                if (SetPasswordView.isPasswordTheSame(currentPasswordEt, newPasswordEt)) {
                    Toast.makeText(mContext, R.string.change_password_error_same, Toast.LENGTH_LONG).show();
                    return;
                }

                if (SetPasswordView.checkInputPassword(mContext, newPasswordEt, confirmPasswordEt)) {
                    if (confirmBtnClickListener != null) {
                        confirmBtnClickListener.onConfirmBtnClick(currentPasswordEt.getText().toString(), newPasswordEt.getText().toString());
                    }
                }
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

    public void updatePasswordCheckError() {
        updateFirstTv(R.string.check_password_error, R.color.error_warn_color);
    }

    private void updateFirstTv(int strId, int colorId) {
        mFirstTv.setText(strId);
        mFirstTv.setTextColor(AppUtils.getColor(mContext, colorId));
    }

    public void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    public interface ConfirmBtnClickListener {
        void onConfirmBtnClick(String oldPassword, String newPassword);
    }
}
