package com.x.wallet.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wuliang on 18-3-27.
 */

public class ContentShowDialogHelper {

    public static void showContentDialog(final Activity activity, final int titleId, final int btnStrId, final String content, final long accountId){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View contentView = inflater.inflate(R.layout.show_account_key_dialog, null);
        builder.setView(contentView);
        final Dialog dialog = builder.create();

        TextView titleTv = contentView.findViewById(R.id.title_tv);
        titleTv.setText(titleId);

        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        final TextView contentTv = contentView.findViewById(R.id.content_tv);
        contentTv.setText(content);

        final Button confirmBtn = contentView.findViewById(R.id.action_btn);
        confirmBtn.setText(btnStrId);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AppUtils.copyContent(activity, R.string.copy_ok, content, "content")){
                    updateHasBackup(ContentUris.withAppendedId(XWalletProvider.CONTENT_URI, accountId));
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

    public static void showConfirmDialog(final Context context, int titleId, String content,
                                         final View.OnClickListener leftBtnClickListener, final View.OnClickListener rightBtnClickListener){
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View contentView = inflater.inflate(R.layout.content_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(contentView);
        final Dialog dialog = builder.create();

        TextView titleTv = contentView.findViewById(R.id.title_tv);
        titleTv.setText(titleId);

        final TextView contentTv = contentView.findViewById(R.id.content_tv);
        contentTv.setText(content);

        final Button leftBtn = contentView.findViewById(R.id.left_btn);
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(leftBtnClickListener != null){
                    leftBtnClickListener.onClick(view);
                }
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });
        final Button rightBtn = contentView.findViewById(R.id.right_btn);
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rightBtnClickListener != null){
                    rightBtnClickListener.onClick(view);
                }
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    public static void updateHasBackup(final Uri uri){
        if(uri != null){
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    int count = DbUtils.updateHasBackup(uri);
                }
            });
        }
    }
}
