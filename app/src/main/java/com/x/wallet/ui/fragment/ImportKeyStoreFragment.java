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
    private EditText mKeyStorePasswordEt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.import_keystore_fragment, container, false);
        initCommonView(view);
        mKeyStoreEt = view.findViewById(R.id.keystore_et);
        mKeyStorePasswordEt = view.findViewById(R.id.keystore_password_et);

        mImportAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean passwordCheckResult = mSetPasswordView.checkInputPassword(ImportKeyStoreFragment.this.getActivity());
                if(!passwordCheckResult){
                    return;
                }
                if(mKeyStoreEt.getText() == null || TextUtils.isEmpty(mKeyStoreEt.getText().toString())){
                    Toast.makeText(ImportKeyStoreFragment.this.getActivity(), R.string.blank_keystore, Toast.LENGTH_LONG).show();
                    return;
                }
                if(mKeyStorePasswordEt.getText() == null || TextUtils.isEmpty(mKeyStorePasswordEt.getText().toString())){
                    Toast.makeText(ImportKeyStoreFragment.this.getActivity(), R.string.blank_keystore_password, Toast.LENGTH_LONG).show();
                    return;
                }
                ImportAddressAsycTask task = new ImportAddressAsycTask(ImportKeyStoreFragment.this.getActivity(),
                        AppUtils.IMPORTTYPE.IMPORT_TYPE_KEYSTORE,
                        mCoinType,
                        mSetPasswordView.getPassword(),
                        mAccountNameView.getAccountName());
                task.setKeyStore(mKeyStoreEt.getText().toString(), mKeyStorePasswordEt.getText().toString());
                task.execute();
            }
        });
        return view;
    }
}
