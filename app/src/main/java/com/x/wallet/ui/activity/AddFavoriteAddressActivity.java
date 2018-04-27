package com.x.wallet.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.transaction.FavoriteAddressDbAsycTask;
import com.x.wallet.transaction.address.AddressUtils;
import com.x.wallet.ui.data.AddressItem;
import com.x.wallet.ui.dialog.ContentShowDialogHelper;

import net.bither.bitherj.utils.Utils;

public class AddFavoriteAddressActivity extends WithBackAppCompatActivity {
    private EditText mAddressEt;
    private ImageButton mScanIb;
    private EditText mAddressTypeEt;
    private EditText mAddressNameEt;
    private Button mFinishButton;
    private AddressItem addressItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_favorite_address_activity);
        if (getIntent().hasExtra(AppUtils.ADDRESS_ITEM)) {
            addressItem = (AddressItem) getIntent().getSerializableExtra(AppUtils.ADDRESS_ITEM);
        }
        initView();
    }

    private void initView() {
        mAddressEt = findViewById(R.id.add_address_et);
        mScanIb = findViewById(R.id.address_scan);
        mAddressTypeEt = findViewById(R.id.address_type);
        mAddressNameEt = findViewById(R.id.address_name_et);
        mFinishButton = findViewById(R.id.finish_add_btn);

        bindData();

        mScanIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddFavoriteAddressActivity.this, ScanAddressQRActivity.class);
                startActivityForResult(intent, ScanAddressQRActivity.REQUEST_CODE);
            }
        });
        mAddressTypeEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseCoinType();
            }
        });

        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(getEditContent(mAddressTypeEt))) {
                    alertErrorMsg(getResources().getString(R.string.choose_coin_type));
                    return;
                }

                if (!isValidAddress(getEditContent(mAddressEt))) {
                    alertErrorMsg(getResources().getString(R.string.invalid_address));
                }

                if (addressItem != null) {
                    handleAddressAction(FavoriteAddressDbAsycTask.UPDATE_ACTION);
                } else {
                    handleAddressAction(FavoriteAddressDbAsycTask.INSERT_ACTION);
                }
            }
        });

        mAddressEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    mFinishButton.setEnabled(false);
                } else {
                    mFinishButton.setEnabled(true);
                    if (AddressUtils.validEthAddress(s.toString())) {
                        mAddressTypeEt.setText(AppUtils.COIN_ARRAY[1]);
                    } else {
                        mAddressTypeEt.setText(AppUtils.COIN_ARRAY[0]);
                    }
                }
            }
        });

    }

    private void bindData() {
        if (addressItem != null) {
            setTitle(R.string.edit_favorite_address);
            mAddressEt.setText(addressItem.getAddress());
            mAddressTypeEt.setText(addressItem.getAddressType());
            mAddressNameEt.setText(addressItem.getName());
            mFinishButton.setEnabled(true);
        } else {
            setTitle(R.string.add_favorite_address);
        }
    }

    private boolean isValidAddress(String address) {
        final String coinType = getEditContent(mAddressTypeEt);
        if (coinType.equals("ETH")) {
            return AddressUtils.validEthAddress(address);
        } else {
            return Utils.validBicoinAddress(address);
        }
    }

    private void chooseCoinType() {
        final String[] items = getResources().getStringArray(R.array.support_coins_array);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.choose_coin_type).setSingleChoiceItems(
                new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, items), -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAddressTypeEt.setText(items[which]);
                        dialog.dismiss();
                    }
                }
        ).create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanAddressQRActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String address = data.getStringExtra(ScanAddressQRActivity.EXTRA_ADDRESS);
                mAddressEt.setText(address);
            }
        }
    }

    private void handleAddressAction(final int actionType) {
        final String address = getEditContent(mAddressEt);
        final String type = getEditContent(mAddressTypeEt);
        final String name = getEditContent(mAddressNameEt);
        long oldId = addressItem != null ? addressItem.getId() : -1;
        new FavoriteAddressDbAsycTask(this, oldId,
                address, type, name, actionType,
                new FavoriteAddressDbAsycTask.OnDataActionFinishedListener() {
                    @Override
                    public void onDataActionFinished(boolean isSuccess) {
                        if (isSuccess) {
                            Toast.makeText(AddFavoriteAddressActivity.this, getSuccessResultText(actionType), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddFavoriteAddressActivity.this, getFailedResultText(actionType), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).execute();
    }

    private String getEditContent(EditText text) {
        if (!TextUtils.isEmpty(text.getText())) {
            return text.getText().toString();
        } else {
            return "";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (addressItem != null) {
            getMenuInflater().inflate(R.menu.del_favorite_address_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_del_favorite_address:
                deleteConfirmDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteConfirmDialog() {
        ContentShowDialogHelper.showConfirmDialog(this, R.string.del_favorite_address, getResources().getString(R.string.confirm_delete_address), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleAddressAction(FavoriteAddressDbAsycTask.DEL_ACTION);
            }
        });
    }

    private void alertErrorMsg(String msg) {
        Toast.makeText(AddFavoriteAddressActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private int getSuccessResultText(int actionType){
        switch (actionType){
            case FavoriteAddressDbAsycTask.INSERT_ACTION:
                return R.string.favorite_address_save_success;
            case FavoriteAddressDbAsycTask.UPDATE_ACTION:
                return R.string.favorite_address_update_success;
            case FavoriteAddressDbAsycTask.DEL_ACTION:
                return R.string.favorite_address_delete_success;
        }
        return R.string.favorite_address_save_success;
    }

    private int getFailedResultText(int actionType){
        switch (actionType){
            case FavoriteAddressDbAsycTask.INSERT_ACTION:
                return R.string.favorite_address_save_failed;
            case FavoriteAddressDbAsycTask.UPDATE_ACTION:
                return R.string.favorite_address_update_failed;
            case FavoriteAddressDbAsycTask.DEL_ACTION:
                return R.string.favorite_address_delete_failed;
        }
        return R.string.favorite_address_save_failed;
    }
}
