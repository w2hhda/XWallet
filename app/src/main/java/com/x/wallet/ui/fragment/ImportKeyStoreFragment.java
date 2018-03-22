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

public class ImportKeyStoreFragment extends BaseImportFragment {
    private EditText mKeyStoreEt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.import_keystore_fragment, container, false);
        initCommonView(view);
        mKeyStoreEt = view.findViewById(R.id.keystore_et);
        mImportAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mSetPasswordView.getPassword())){
                    Toast.makeText(ImportKeyStoreFragment.this.getActivity(), R.string.password_error_blank, Toast.LENGTH_LONG).show();
                    return;
                }
                if(mKeyStoreEt.getText() == null || TextUtils.isEmpty(mKeyStoreEt.getText().toString())){
                    Toast.makeText(ImportKeyStoreFragment.this.getActivity(), R.string.blank_key, Toast.LENGTH_LONG).show();
                    return;
                }
                new ImportAddressAsycTask(ImportKeyStoreFragment.this.getActivity(), mCoinType, AppUtils.IMPORTTYPE.IMPORT_TYPE_KEYSTORE,
                        mSetPasswordView.getPassword(),
                        mAccountNameView.getAccountName(),
                        mKeyStoreEt.getText().toString()).execute();
            }
        });
        return view;
    }
}
