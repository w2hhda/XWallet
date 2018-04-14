package com.x.wallet.ui.data;

import android.database.Cursor;
import android.text.TextUtils;

import com.x.wallet.lib.eth.data.TransactionsResultBean;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by Nick on 27/3/2018.
 */

public class TransactionItem implements Serializable {
    public final static String TRANSACTION_TYPE_RECEIVE = "is_receive";
    public final static String TRANSACTION_TYPE_TRANSFER_OUT = "is_transfer_out";

    private String toAddress;
    private String fromAddress;
    private String receiptHash;
    private String amount;
    private String transactionType;
    private String mCoinType;
    private String timeStamp;
    private String transactionFee;
    private String nonce;
    private String tokenSymbols;
    private String tokenDecimals;
    private Boolean isToken = false;
    private Boolean isError = false;
    private int txReceiptStatus;

    public TransactionItem() {
    }

    public TransactionItem(String toAddress, String fromAddress, String receiptHash, String amount, String transactionType, String mCoinType, String timeStamp, String transactionFee, String nonce) {
        this.toAddress = toAddress;
        this.fromAddress = fromAddress;
        this.receiptHash = receiptHash;
        this.amount = amount;
        this.transactionType = transactionType;
        this.mCoinType = mCoinType;
        this.timeStamp = timeStamp;
        this.transactionFee = transactionFee;
        this.nonce = nonce;
    }

    public static TransactionItem createFromReceipt(TransactionsResultBean.ReceiptBean receipts, Boolean isReceive, Boolean isToken){
        TransactionItem item = new TransactionItem();
        item.setToAddress(receipts.getTo());
        item.setFromAddress(receipts.getFrom());
        item.setReceiptHash(receipts.getHash());
        item.setTimeStamp(receipts.getTimeStamp());
        BigInteger gasUsed = new BigInteger(receipts.getGasUsed());
        BigInteger gasPrice = new BigInteger(receipts.getGasPrice());
        item.setTransactionFee(gasUsed.multiply(gasPrice).toString());
        item.setNonce(receipts.getNonce());
        item.setError(receipts.getIsError() == 1);
        item.setTxReceiptStatus(receipts.getTxreceipt_status());
        if (isReceive){
            item.setTransactionType(TRANSACTION_TYPE_RECEIVE);
        }else {
            item.setTransactionType(TRANSACTION_TYPE_TRANSFER_OUT);
        }

        if (isToken){
            item.setAmount(gasPrice.multiply(gasUsed).toString());
            item.setToken(true);
        }else {
            item.setAmount(receipts.getValue());
            item.setToken(false);
        }

        return item;

    }

    private static final int TX_HASH            = 1;
    private static final int TIME_STAMP         = 2;
    private static final int NONCE              = 3;
    private static final int FROM_ADDRESS       = 4;
    private static final int TO_ADDRESS         = 5;
    private static final int VALUE              = 6;
    private static final int GAS_LIMIT          = 7;
    private static final int GAS_PRICE          = 8;
    private static final int IS_ERROR           = 9;
    private static final int TX_RECEIPT_STATUS  = 10;
    private static final int INPUT_DATA         = 11;
    private static final int GAS_USED           = 12;
    private static final int CONTRACT_ADDRESS   = 13;
    private static final int TOKEN_SYMBOL       = 14;
    private static final int TOKEN_NAME         = 15;
    private static final int TOKEN_DECIMALS     = 16;
    private static final int BLOCK_NUMBER       = 17;
    public static TransactionItem createFromCursor(Cursor cursor, String address, boolean isTokenAccount){
        TransactionItem item = new TransactionItem();
        item.setToAddress(cursor.getString(TO_ADDRESS));
        item.setFromAddress(cursor.getString(FROM_ADDRESS));
        item.setReceiptHash(cursor.getString(TX_HASH));
        item.setTimeStamp(cursor.getString(TIME_STAMP));
        item.setNonce(cursor.getString(NONCE));

        //token tx haven't tx_receipt_status now
        if (!TextUtils.isEmpty(cursor.getString(TX_RECEIPT_STATUS))){
            item.setTxReceiptStatus(Integer.parseInt(cursor.getString(TX_RECEIPT_STATUS)));
        }

        //check transfer or receive
        if (address.equalsIgnoreCase(cursor.getString(TO_ADDRESS))){
            item.setTransactionType(TRANSACTION_TYPE_RECEIVE);
        }else if (address.equalsIgnoreCase(cursor.getString(FROM_ADDRESS))){
            item.setTransactionType(TRANSACTION_TYPE_TRANSFER_OUT);
        }

        //item.setToken(isTokenAccount);
        String contract = cursor.getString(CONTRACT_ADDRESS);
        if (TextUtils.isEmpty(contract)){
            item.setToken(false);
        }else {
            item.setToken(true);
        }


        String gasPrice = cursor.getString(GAS_PRICE);
        String gasLimit = cursor.getString(GAS_LIMIT);
        String gasUsed  = cursor.getString(GAS_USED);

        if (TextUtils.isEmpty(gasUsed)){
            item.setTransactionFee(new BigInteger(gasPrice).multiply(new BigInteger(gasLimit)).toString());
        }else {
            item.setTransactionFee(new BigInteger(gasPrice).multiply(new BigInteger(gasUsed)).toString());
        }

        if (isTokenAccount){
            item.setTokenSymbols(cursor.getString(TOKEN_SYMBOL));
            item.setTokenDecimals(cursor.getString(TOKEN_DECIMALS));
        } else {
            item.setTokenSymbols("ETH");
            item.setTokenDecimals("18");

        }

        String isError = cursor.getString(IS_ERROR);
        if (isError != null && isError.equals("0")){
            item.setError(false);
            //token show fee in eth list.
            if (!isTokenAccount && !TextUtils.isEmpty(cursor.getString(CONTRACT_ADDRESS))){
                item.setAmount(item.getTransactionFee());
            }else {
                item.setAmount(cursor.getString(VALUE));
            }
        }else {
            item.setError(true);
            if (!isTokenAccount) { //when error, show the transactionFee
                item.setAmount(item.getTransactionFee());
            }else {
                item.setAmount(cursor.getString(VALUE));
            }
        }

        return item;
    }

    public String getTokenDecimals() {
        return tokenDecimals;
    }

    public void setTokenDecimals(String tokenDecimals) {
        this.tokenDecimals = tokenDecimals;
    }

    public String getTokenSymbols() {
        return tokenSymbols;
    }

    public void setTokenSymbols(String tokenSymbols) {
        this.tokenSymbols = tokenSymbols;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getReceiptHash() {
        return receiptHash;
    }

    public void setReceiptHash(String receiptHash) {
        this.receiptHash = receiptHash;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getmCoinType() {
        return mCoinType;
    }

    public void setmCoinType(String mCoinType) {
        this.mCoinType = mCoinType;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(String transactionFee) {
        this.transactionFee = transactionFee;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public Boolean getToken() {
        return isToken;
    }

    public void setToken(Boolean token) {
        isToken = token;
    }

    public Boolean getError() {
        return isError;
    }

    public void setError(Boolean error) {
        isError = error;
    }

    public int getTxReceiptStatus() {
        return txReceiptStatus;
    }

    public void setTxReceiptStatus(int txReceiptStatus) {
        this.txReceiptStatus = txReceiptStatus;
    }

    @Override
    public String toString() {
        return "TransactionItem{" +
                "toAddress='" + toAddress + '\'' +
                ", fromAddress='" + fromAddress + '\'' +
                ", receiptHash='" + receiptHash + '\'' +
                ", amount='" + amount + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", mCoinType='" + mCoinType + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", transactionFee='" + transactionFee + '\'' +
                ", nonce='" + nonce + '\'' +
                ", isToken='" + isToken + '\'' +
                '}';
    }
}
