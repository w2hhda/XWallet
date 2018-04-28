package com.x.wallet.transaction.transfer;

public interface ConfirmTransactionCallback<T> {
    void onTransactionConfirmed(T result, Throwable e);
}
