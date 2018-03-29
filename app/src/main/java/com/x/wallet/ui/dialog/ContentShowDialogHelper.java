package com.x.wallet.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.R;

/**
 * Created by wuliang on 18-3-27.
 */

public class ContentShowDialogHelper {

    public static void showContentDialog(final Activity activity, final int titleId, final int btnStrId, final String content){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View contentView = inflater.inflate(R.layout.content_show_dialog, null);
        builder.setView(contentView);
        final Dialog dialog = builder.create();

        TextView titleTv = contentView.findViewById(R.id.title_tv);
        titleTv.setText(titleId);

        final TextView contentTv = contentView.findViewById(R.id.content_tv);
        contentTv.setText(content);

        final Button confirmBtn = contentView.findViewById(R.id.action_btn);
        confirmBtn.setText(btnStrId);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                if(cm != null){
                    ClipData clip = ClipData.newPlainText("content",  content);
                    cm.setPrimaryClip(clip);
                    Toast.makeText(activity, R.string.copy_ok, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, R.string.copy_failed, Toast.LENGTH_LONG).show();
                }
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }
}
