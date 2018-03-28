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
                boolean passwordCheckResult = mSetPasswordView.checkInputPassword(ImportKeyFragment.this.getActivity());
                if(!passwordCheckResult){
                    return;
                }
                if(mKeyEt.getText() == null || TextUtils.isEmpty(mKeyEt.getText().toString())){
                    Toast.makeText(ImportKeyFragment.this.getActivity(), R.string.blank_key, Toast.LENGTH_LONG).show();
                    return;
                }
                ImportAddressAsycTask task = new ImportAddressAsycTask(ImportKeyFragment.this.getActivity(),
                        AppUtils.IMPORTTYPE.IMPORT_TYPE_KEY,
                        mCoinType,
                        mSetPasswordView.getPassword(),
                        mAccountNameView.getAccountName());
                task.setKey(mKeyEt.getText().toString());
                task.execute();

            }
        });
        return view;
    }
}
