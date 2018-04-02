package com.x.wallet.ui.data;

import android.util.Log;

import com.x.wallet.lib.eth.EthUtils;
import com.x.wallet.lib.eth.data.TransactionsResultBean;

import java.io.Serializable;
import java.math.BigDecimal;
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
    private String transactionFax;
    private String nonce;
    private Boolean isToken = false;
    private Boolean isError = false;
    private int txReceiptStatus;

    public TransactionItem() {
    }

    public TransactionItem(String toAddress, String fromAddress, String receiptHash, String amount, String transactionType, String mCoinType, String timeStamp, String transactionFax, String nonce) {
        this.toAddress = toAddress;
        this.fromAddress = fromAddress;
        this.receiptHash = receiptHash;
        this.amount = amount;
        this.transactionType = transactionType;
        this.mCoinType = mCoinType;
        this.timeStamp = timeStamp;
        this.transactionFax = transactionFax;
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
        item.setTransactionFax(gasUsed.multiply(gasPrice).toString());
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

    public String getTransactionFax() {
        return transactionFax;
    }

    public void setTransactionFax(String transactionFax) {
        this.transactionFax = transactionFax;
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
                ", transactionFax='" + transactionFax + '\'' +
                ", nonce='" + nonce + '\'' +
                ", isToken='" + isToken + '\'' +
                '}';
    }
}
