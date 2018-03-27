package com.x.wallet.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
    public static void showPasswordDialog(Activity activity, final ConfirmBtnClickListener confirmBtnClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View contentView = inflater.inflate(R.layout.password_confirm_dialog, null);
        builder.setView(contentView);
        final Dialog dialog = builder.create();
        final TextView firstTv = contentView.findViewById(R.id.tv);
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
                firstTv.setText(R.string.confirm_password_to_get_mnemonic);
                confirmBtn.setEnabled(s != null && s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean result = confirmBtnClickListener.onConfirmBtnClick(passwordEt.getText() != null ? passwordEt.getText().toString() : "");
                if(result){
                    if(dialog != null){
                        dialog.dismiss();
                    }
                } else {
                    firstTv.setText(R.string.check_password_error);
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    public interface ConfirmBtnClickListener{
        boolean onConfirmBtnClick(String password);
    }
}
