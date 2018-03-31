package com.x.wallet.ui.data;

/**
 * Created by wuliang on 18-3-30.
 */

public class TokenItemBean {
    private int idInAll;
    private String name;
    private String symbol;
    private int decimals;
    private String contractAddress;
    private int orderInAll;

    public TokenItemBean(int idInAll, String name, String symbol, int decimals, String contractAddress, int orderInAll) {
        this.idInAll = idInAll;
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
        this.contractAddress = contractAddress;
        this.orderInAll = orderInAll;
    }

    public int getIdInAll() {
        return idInAll;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimals() {
        return decimals;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public int getOrderInAll() {
        return orderInAll;
    }
}
