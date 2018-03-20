package com.xwallet.ethwallet.data;

import java.math.BigInteger;

public class EthAccountData {

    private String ethAccountName;
    private String mAddress;
    private String keyStore;
    private String mEncryptMnemonic;
    private BigInteger balance;
    private String mCoinName;

    public EthAccountData(String mAddress, String keyStore, String mEncryptMnemonic) {
        this.mAddress = mAddress;
        this.keyStore = keyStore;
        this.mEncryptMnemonic = mEncryptMnemonic;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getmEncryptMnemonic() {
        return mEncryptMnemonic;
    }

    public void setmEncryptMnemonic(String mEncryptMnemonic) {
        this.mEncryptMnemonic = mEncryptMnemonic;
    }

    public String getCoinName() {
        return mCoinName;
    }

    public void setCoinName(String coinName) {
        this.mCoinName = coinName;
    }

    public BigInteger getBalanceNative() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance.doubleValue();
        //return new BigDecimal(balance).divide(ExchangeCalculator.ONE_ETHER, 8, BigDecimal.ROUND_UP).doubleValue();
    }

    public String getEthAccountName() {
        return ethAccountName;
    }

    public void setEthAccountName(String ethAccountName) {
        this.ethAccountName = ethAccountName;
    }

    public String getmAddress() {
        return mAddress.toLowerCase();
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }

}
