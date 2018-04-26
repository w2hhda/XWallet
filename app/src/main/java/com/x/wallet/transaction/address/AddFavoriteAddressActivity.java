package com.x.wallet.transaction.address;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.db.DbUtils;
import com.x.wallet.ui.activity.ScanAddressQRActivity;
import com.x.wallet.ui.activity.WithBackAppCompatActivity;
import com.x.wallet.ui.data.AddressItem;
import com.x.wallet.ui.dialog.ContentShowDialogHelper;

import net.bither.bitherj.utils.Utils;

public class AddFavoriteAddressActivity extends WithBackAppCompatActivity {
    private ImageButton mScanIb;
    private EditText mAddressType;
    private EditText mAddressName;
    private EditText mAddressEt;
    private Button mFinishButton;

    private AddressItem addressItem;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_favorite_address_activity);
        if (getIntent().hasExtra(AppUtils.ADDRESS_ITEM)){
            addressItem = (AddressItem) getIntent().getSerializableExtra(AppUtils.ADDRESS_ITEM);
        }
        initView();
    }

    private void initView(){
        mScanIb = findViewById(R.id.address_scan);
        mAddressType = findViewById(R.id.address_type);
        mAddressName = findViewById(R.id.address_name_et);
        mAddressEt   = findViewById(R.id.add_address_et);
        mFinishButton = findViewById(R.id.finish_add_btn);
        setViewContent();

        mScanIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddFavoriteAddressActivity.this, ScanAddressQRActivity.class);
                startActivityForResult(intent, ScanAddressQRActivity.REQUEST_CODE);
            }
        });
        mAddressType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseCoinType();
            }
        });

        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(getContent(mAddressType))){
                    alertErrorMsg(getResources().getString(R.string.choose_coin_type));
                }else if (checkValidAddress(getContent(mAddressEt))) {
                    insertAddressToDb();
                }else {
                    alertErrorMsg(getResources().getString(R.string.invalid_address));
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
                if (TextUtils.isEmpty(s)){
                    mFinishButton.setEnabled(false);
                }else {
                    mFinishButton.setEnabled(true);
                    if (AddressUtils.validEthAddress(s.toString())){
                        mAddressType.setText(AppUtils.COIN_ARRAY[1]);
                    }else {
                        mAddressType.setText(AppUtils.COIN_ARRAY[0]);
                    }
                }
            }
        });

    }

    private void setViewContent(){
        if (addressItem != null){
            setTitle(R.string.edit_favorite_address);
            mAddressName.setText(addressItem.getName());
            mAddressEt.setText(addressItem.getAddress());
            mAddressType.setText(addressItem.getAddressType());
            mFinishButton.setEnabled(true);

        }else {
            setTitle(R.string.add_favorite_address);
        }
    }

    private boolean checkValidAddress(String address){
        final String coinType = getContent(mAddressType);
        if (coinType.equals("ETH")) {
            return AddressUtils.validEthAddress(address);
        }else {
            return Utils.validBicoinAddress(address);
        }
    }

    private void chooseCoinType(){
        final String[] items = getResources().getStringArray(R.array.support_coins_array);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.choose_coin_type).setSingleChoiceItems(
                new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, items), -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAddressType.setText(items[which]);
                        dialog.dismiss();
                    }
                }
        ).create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanAddressQRActivity.REQUEST_CODE){
            if (resultCode == RESULT_OK){
                String address = data.getStringExtra(ScanAddressQRActivity.EXTRA_ADDRESS);
                mAddressEt.setText(address);
            }
        }
    }

    private void insertAddressToDb(){
        final String name = getContent(mAddressName);
        final String address = getContent(mAddressEt);
        final String type = getContent(mAddressType);
        final ChangeDbCallback callback = new ChangeDbCallback() {
            @Override
            public void changeFinished(final boolean isOk) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isOk){
                            Toast.makeText(AddFavoriteAddressActivity.this, "save ok", Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            Toast.makeText(AddFavoriteAddressActivity.this, "save fail, existed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        new Thread(new ChangeDbRunnable(address, name, type, ChangeDbRunnable.INSERT_ACTION ,callback)).start();

    }

    private void updateAddress(){
        final String name = getContent(mAddressName);
        final String address = getContent(mAddressEt);
        final String type = getContent(mAddressType);
        final ChangeDbCallback callback = new ChangeDbCallback() {
            @Override
            public void changeFinished(final boolean isOk) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isOk){
                            Toast.makeText(AddFavoriteAddressActivity.this, "update ok", Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            Toast.makeText(AddFavoriteAddressActivity.this, "add fail, existed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        new Thread(new ChangeDbRunnable(address, name, type, ChangeDbRunnable.UPDATE_ACTION ,callback)).start();
    }

    private void delAddress(){
        final String address = getContent(mAddressEt);
        final ChangeDbCallback callback = new ChangeDbCallback() {
            @Override
            public void changeFinished(final boolean isOk) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isOk){
                            finish();
                        }
                    }
                });
            }
        };
        new Thread(new ChangeDbRunnable(address, null, null, ChangeDbRunnable.DEL_ACTION, callback)).start();
    }

    private void deleteConfirmDialog(){
        ContentShowDialogHelper.showConfirmDialog(this, R.string.del_favorite_address, getResources().getString(R.string.confirm_delete_address), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delAddress();
            }
        });
    }

    private class ChangeDbRunnable implements Runnable{
        public static final String INSERT_ACTION = "insert_action";
        public static final String DEL_ACTION = "delete_action";
        public static final String UPDATE_ACTION = "update_action";
        private String address;
        private String name;
        private String type;
        private String actionType;
        private ChangeDbCallback callback;
        public ChangeDbRunnable(String address, String name, String type, String actionType, ChangeDbCallback callback) {
            this.address = address;
            this.name = name;
            this.type = type;
            this.callback = callback;
            this.actionType = actionType;
        }

        @Override
        public void run() {
            switch (actionType){
                case INSERT_ACTION:
                    DbUtils.insertFavoriteAddressIntoDb(address, type, name, callback);
                    break;
                case DEL_ACTION:
                    DbUtils.deleteFavoriteAddress(address, callback);
                    break;
                case UPDATE_ACTION:
                    DbUtils.updateFavoriteAddress(address, type, name, callback);
                    break;
            }
        }
    }

    private String getContent(EditText text){
        if (!TextUtils.isEmpty(text.getText())){
            return text.getText().toString();
        }else {
            return "";
        }
    }

    public interface ChangeDbCallback {
        void changeFinished(boolean isOk);
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
        switch (item.getItemId()){
            case R.id.action_del_favorite_address:
                deleteConfirmDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void alertErrorMsg(String msg){
        Toast.makeText(AddFavoriteAddressActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
