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

public class ImportMnemonicFragment extends BaseImportFragment {
    private EditText mMnemonicEt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.import_mnemonic_fragment, container, false);
        initCommonView(view);

        mMnemonicEt = view.findViewById(R.id.mnemonic_et);
        mImportAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean passwordCheckResult = mSetPasswordView.checkInputPassword(ImportMnemonicFragment.this.getActivity());
                if(!passwordCheckResult){
                    return;
                }

                if(mMnemonicEt.getText() == null || TextUtils.isEmpty(mMnemonicEt.getText().toString())){
                    Toast.makeText(ImportMnemonicFragment.this.getActivity(), R.string.blank_mnemonic, Toast.LENGTH_LONG).show();
                    return;
                }
                ImportAddressAsycTask task = new ImportAddressAsycTask(ImportMnemonicFragment.this.getActivity(),
                        AppUtils.IMPORTTYPE.IMPORT_TYPE_MNEMONIC,
                        mCoinType,
                        mSetPasswordView.getPassword(),
                        mAccountNameView.getAccountName());
                task.setMnemonic(mMnemonicEt.getText().toString());
                task.execute();
            }
        });
        return view;
    }
}
