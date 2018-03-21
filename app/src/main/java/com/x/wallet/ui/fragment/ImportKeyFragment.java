package com.x.wallet.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.transaction.address.ImportAddressAsycTask;

/**
 * Created by wuliang on 18-3-16.
 */

public class ImportKeyFragment extends BaseImportFragment {
    private EditText mKeyEt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.import_key_fragment, container, false);
        initCommonView(view);
        mKeyEt = view.findViewById(R.id.key_et);
        mImportAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mSetPasswordView.getPassword())){
                    Toast.makeText(ImportKeyFragment.this.getActivity(), R.string.password_error_blank, Toast.LENGTH_LONG).show();
                    return;
                }
                if(mKeyEt.getText() == null || TextUtils.isEmpty(mKeyEt.getText().toString())){
                    Toast.makeText(ImportKeyFragment.this.getActivity(), R.string.blank_key, Toast.LENGTH_LONG).show();
                    return;
                }
                new ImportAddressAsycTask(ImportKeyFragment.this.getActivity(), mCoinType, AppUtils.IMPORTTYPE.IMPORT_TYPE_KEY,
                        mSetPasswordView.getPassword(),
                        mAccountNameView.getAccountName(),
                        mKeyEt.getText().toString()).execute();
            }
        });
        return view;
    }
}
