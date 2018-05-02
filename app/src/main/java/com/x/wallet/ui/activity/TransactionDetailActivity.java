package com.x.wallet.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.x.wallet.AppUtils;
import com.x.wallet.R;
import com.x.wallet.btc.BtcUtils;
import com.x.wallet.lib.common.LibUtils;
import com.x.wallet.transaction.FavoriteAddressDbAsycTask;
import com.x.wallet.transaction.token.TokenUtils;
import com.x.wallet.ui.ActionUtils;
import com.x.wallet.ui.data.AddressItem;
import com.x.wallet.ui.data.TransactionItem;
import com.x.wallet.ui.helper.FavoriteAddressHelper;

import java.math.BigDecimal;

/**
 * Created by Nick on 28/3/2018.
 */

public class TransactionDetailActivity extends WithBackAppCompatActivity {
    private TextView addOutAddressFavoriteTv;
    private ImageView addOutAddressIv;
    private TextView toAddressTv;
    private TextView fromAddressTv;
    private TextView addReceivedFavoriteTv;
    private ImageView addReceivedIv;
    private TransactionItem item;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_detail_activity);
        setTitle(getResources().getString(R.string.transaction_detail));
        initView();
    }

    private void initView(){
        fromAddressTv = findViewById(R.id.transaction_detail_from);
        toAddressTv   = findViewById(R.id.transaction_detail_to);
        TextView amountTv      = findViewById(R.id.transaction_detail_amount);
        TextView feeTv         = findViewById(R.id.transaction_detail_fax);
        TextView hashTv        = findViewById(R.id.transaction_detail_hash);
        addOutAddressFavoriteTv = findViewById(R.id.add_to_favorite_to_address);
        addOutAddressIv = findViewById(R.id.to_icon_to_address);
        addReceivedFavoriteTv = findViewById(R.id.add_to_favorite_from_address);
        addReceivedIv = findViewById(R.id.to_icon_from_address);
        item = (TransactionItem) getIntent().getSerializableExtra(AppUtils.TRANSACTION_ITEM);
        boolean isTokenAccount = getIntent().getBooleanExtra(AppUtils.ACCOUNT_TYPE, false);

        fromAddressTv.setText(item.getFromAddress());
        toAddressTv.setText(item.getToAddress());

        if(isBtcAccount()){
            bindAmount(item, amountTv, feeTv);
        } else {
            bindAmount(item, isTokenAccount, amountTv, feeTv);
        }

        hashTv.setText(item.getReceiptHash());
        initAddView();
        bindAddView();
    }

    private void bindAmount(TransactionItem item, boolean isTokenAccount, TextView amountTv, TextView feeTv){
        BigDecimal rawAmount = new BigDecimal(item.getAmount());
        int decimal = 18;
        if (isTokenAccount){
            decimal = Integer.parseInt(item.getTokenDecimals());
        }else if (item.getToken() || item.getError()){
            decimal = 18;
        }

        String amountResult = rawAmount.divide(BigDecimal.TEN.pow(decimal)).setScale(6,BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString();
        //String amountResult = ExchangeCalUtil.getInstance().weiToEther(new BigDecimal(item.getAmount())).stripTrailingZeros().toPlainString();
        String symbols = "ETH";

        if (isTokenAccount){
            symbols = item.getTokenSymbols();
        }else if (item.getError() || item.getToken()){
            if (item.getTokenSymbols() != null) {
                symbols = item.getTokenSymbols();
            }
        }else {
            symbols = item.getmCoinType();
        }

        String amountString = getPrefix(item.getTransactionType()) + amountResult + " " + symbols;
        amountTv.setText(amountString);

        feeTv.setText(TokenUtils.getBalanceText(item.getTransactionFee(), TokenUtils.ETH_DECIMALS) + " ETH");
    }

    private void bindAmount(TransactionItem item, TextView amountTv, TextView feeTv){
        String prefix = getPrefix(item.getTransactionType());
        amountTv.setText(prefix + TokenUtils.getBalanceText(item.getAmount(), BtcUtils.BTC_DECIMALS_COUNT) + " BTC");
        feeTv.setText(TokenUtils.getBalanceText(item.getTransactionFee(), BtcUtils.BTC_DECIMALS_COUNT) + " BTC");
    }

    private String getPrefix(String transactionType){
        return transactionType.equals(TransactionItem.TRANSACTION_TYPE_RECEIVE) ? "+ " : "- ";
    }

    private boolean isBtcAccount(){
        return getIntent().getIntExtra(AppUtils.COIN_TYPE, -1) == LibUtils.COINTYPE.COIN_BTC;
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindAddView();
    }

    final FavoriteAddressDbAsycTask.OnDataActionFinishedListener outListener = new FavoriteAddressDbAsycTask.OnDataActionFinishedListener() {
        @Override
        public void onDataActionFinished(boolean isSuccess) {
            if (!isSuccess) {
                addOutAddressIv.setVisibility(View.VISIBLE);
                addOutAddressFavoriteTv.setVisibility(View.VISIBLE);
            }else {
                addOutAddressIv.setVisibility(View.GONE);
                addOutAddressFavoriteTv.setVisibility(View.GONE);
            }
        }
    };

    final FavoriteAddressDbAsycTask.OnDataActionFinishedListener receivedListener = new FavoriteAddressDbAsycTask.OnDataActionFinishedListener() {
        @Override
        public void onDataActionFinished(boolean isSuccess) {
            if (!isSuccess) {
                addReceivedIv.setVisibility(View.VISIBLE);
                addReceivedFavoriteTv.setVisibility(View.VISIBLE);
            }else {
                addReceivedIv.setVisibility(View.GONE);
                addReceivedFavoriteTv.setVisibility(View.GONE);
            }
        }
    };

    private void initAddView(){
        final String type = isBtcAccount() ? AppUtils.COIN_ARRAY[0] : AppUtils.COIN_ARRAY[1];
        if (isReceiveTx()){
            final String address = fromAddressTv.getText().toString();
            addReceivedFavoriteTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActionUtils.addFavoriteAddress(TransactionDetailActivity.this, address, type);
                }
            });
        }else {
            final String address = toAddressTv.getText().toString();
            addOutAddressFavoriteTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActionUtils.addFavoriteAddress(TransactionDetailActivity.this, address, type);
                }
            });
        }
    }

    private void bindAddView(){
        final String type = isBtcAccount() ? AppUtils.COIN_ARRAY[0] : AppUtils.COIN_ARRAY[1];
        String address;
        FavoriteAddressDbAsycTask.OnDataActionFinishedListener listener;
        if (isReceiveTx()){
            address = fromAddressTv.getText().toString();
            listener = receivedListener;
        }else {
            address = toAddressTv.getText().toString();
            listener = outListener;
        }
        final AddressItem addressItem = new AddressItem(-1, address, type, null);
        final int actionType = FavoriteAddressDbAsycTask.QUERY_EXIST;
        FavoriteAddressHelper.handleAddressAction(TransactionDetailActivity.this, actionType, addressItem, listener);
    }

    private boolean isReceiveTx(){
        if (item == null){
            return false;
        }
        return item.getTransactionType().equals(TransactionItem.TRANSACTION_TYPE_RECEIVE);
    }

}
