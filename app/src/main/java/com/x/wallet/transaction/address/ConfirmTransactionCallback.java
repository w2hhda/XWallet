package com.x.wallet.transaction.address;

public interface ConfirmTransactionCallback<T> {
    void onTransactionConfirmed(T result, Throwable e);
}
