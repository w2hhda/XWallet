package com.x.wallet.ui.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.x.wallet.AppUtils;
import com.x.wallet.R;

/**
 * Created by wuliang on 18-3-13.
 */

public class AccountListFragment extends Fragment {
    private View mAddAccountView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment, container, false);
        mAddAccountView = view.findViewById(R.id.add_account_btn);
        mAddAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountListFragment.this.getContext());
                builder.setItems(AccountListFragment.this.getResources().getStringArray(R.array.account_action_array), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        Intent intent = new Intent("com.x.wallet.action.COINTYPE_CHOOSE_ACTION");
                        switch (which){
                            case 0:
                                intent.putExtra("action_type", AppUtils.ACCOUNT_ACTION_TYPE_NEW);
                                break;
                            case 1:
                                intent.putExtra("action_type", AppUtils.ACCOUNT_ACTION_TYPE_IMPORT);
                                break;
                        }
                        AccountListFragment.this.startActivity(intent);
                    }
                });
                builder.setTitle(R.string.add_account);
                builder.show();
            }
        });
        return view;
    }
}
