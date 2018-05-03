package com.x.wallet.ui.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.btc.BtcUtils;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.ui.ActionUtils;
import com.x.wallet.ui.data.TransactionItem;
import com.x.wallet.ui.view.AddToUsualUseAddressView;

import java.math.BigDecimal;

/**
 * Created by Nick on 28/3/2018.
 */

public class TransactionDetailActivity extends WithBackAppCompatActivity {
    private AddToUsualUseAddressView mAddToUsualUseAddressView;

    private TransactionItem mTransactionItem;
    private boolean mIsTokenAccount;

    private LoaderManager mLoaderManager;
    private static final int ADDRESS_IN_USUAL_LOADER = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_detail_activity);
        initData();
        initView();
    }

    private void initData(){
        mTransactionItem = (TransactionItem) getIntent().getSerializableExtra(AppUtils.TRANSACTION_ITEM);
        mIsTokenAccount = getIntent().getBooleanExtra(AppUtils.ACCOUNT_TYPE, false);
    }

    private void initView(){
        TextView fromAddressTv = findViewById(R.id.transaction_detail_from);
        fromAddressTv.setText(mTransactionItem.getFromAddress());

        TextView toAddressTv   = findViewById(R.id.transaction_detail_to);
        toAddressTv.setText(mTransactionItem.getToAddress());

        TextView hashTv = findViewById(R.id.transaction_detail_hash);
        hashTv.setText(mTransactionItem.getReceiptHash());

        TextView amountTv = findViewById(R.id.transaction_detail_amount);
        TextView feeTv = findViewById(R.id.transaction_detail_fax);
        if(isBtcAccount()){
            bindBtcAmount(amountTv, feeTv);
        } else {
            bindAmount(amountTv, feeTv);
        }

        initAddAddressView();
    }

    private void bindAmount(TextView amountTv, TextView feeTv){
        BigDecimal rawAmount = new BigDecimal(mTransactionItem.getAmount());
        int decimal = 18;
        if (mIsTokenAccount){
            decimal = Integer.parseInt(mTransactionItem.getTokenDecimals());
        }else if (mTransactionItem.getToken() || mTransactionItem.getError()){
            decimal = 18;
        }

        String amountResult = rawAmount.divide(BigDecimal.TEN.pow(decimal)).setScale(6,BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString();
        //String amountResult = ExchangeCalUtil.getInstance().weiToEther(new BigDecimal(item.getAmount())).stripTrailingZeros().toPlainString();
        String symbols = "ETH";

        if (mIsTokenAccount){
            symbols = mTransactionItem.getTokenSymbols();
        }else if (mTransactionItem.getError() || mTransactionItem.getToken()){
            if (mTransactionItem.getTokenSymbols() != null) {
                symbols = mTransactionItem.getTokenSymbols();
            }
        }else {
            symbols = mTransactionItem.getmCoinType();
        }

        String amountString = getPrefix(mTransactionItem.getTransactionType()) + amountResult + " " + symbols;
        amountTv.setText(amountString);

        feeTv.setText(TokenUtils.getBalanceText(mTransactionItem.getTransactionFee(), TokenUtils.ETH_DECIMALS) + " ETH");
    }

    private void bindBtcAmount(TextView amountTv, TextView feeTv){
        String prefix = getPrefix(mTransactionItem.getTransactionType());
        amountTv.setText(prefix + TokenUtils.getBalanceText(mTransactionItem.getAmount(), BtcUtils.BTC_DECIMALS_COUNT) + " BTC");
        feeTv.setText(TokenUtils.getBalanceText(mTransactionItem.getTransactionFee(), BtcUtils.BTC_DECIMALS_COUNT) + " BTC");
    }

    private String getPrefix(String transactionType){
        return transactionType.equals(TransactionItem.TRANSACTION_TYPE_RECEIVE) ? "+ " : "- ";
    }

    private boolean isBtcAccount(){
        return getIntent().getIntExtra(AppUtils.COIN_TYPE, -1) == LibUtils.COINTYPE.COIN_BTC;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoaderManager != null){
            mLoaderManager.destroyLoader(ADDRESS_IN_USUAL_LOADER);
            mLoaderManager = null;
        }
    }

    private void initAddAddressView(){
        final String type = isBtcAccount() ? AppUtils.COIN_ARRAY[0] : AppUtils.COIN_ARRAY[1];
        final String address;
        if (isReceiveTx()){
            mAddToUsualUseAddressView = findViewById(R.id.add_from_to_usual_address_view);
            address = mTransactionItem.getFromAddress();
        }else {
            mAddToUsualUseAddressView = findViewById(R.id.add_to_to_usual_address_view);
            address = mTransactionItem.getToAddress();
        }
        mAddToUsualUseAddressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionUtils.addFavoriteAddress(TransactionDetailActivity.this, address, type);
            }
        });

        mLoaderManager = getLoaderManager();
        Bundle bundle = new Bundle();
        bundle.putString(AppUtils.EXTRA_ADDRESS, address);
        mLoaderManager.initLoader(ADDRESS_IN_USUAL_LOADER, bundle, new UsualAddressLoaderCallbacks());
    }

    private boolean isReceiveTx(){
        if (mTransactionItem == null){
            return false;
        }
        return mTransactionItem.getTransactionType().equals(TransactionItem.TRANSACTION_TYPE_RECEIVE);
    }

    private class UsualAddressLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            String address = bundle.getString(AppUtils.EXTRA_ADDRESS);
            return new CursorLoader(TransactionDetailActivity.this, XWalletProvider.CONTENT_URI_ADDRESS,
                    null, DbUtils.AddressTableColumns.ADDRESS + " = ?", new String[]{address}, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if(cursor != null){
                Log.i(AppUtils.APP_TAG, "TransactionDetailActivity onLoadFinished cursor.count = " + cursor.getCount());
                mAddToUsualUseAddressView.setVisibility(cursor.getCount() > 0 ? View.GONE : View.VISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

}
