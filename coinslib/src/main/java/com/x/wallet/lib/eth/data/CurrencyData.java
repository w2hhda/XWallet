package com.x.wallet.lib.eth.data;

/**
 * Created by zhangxing on 18-3-21.
 */

public class CurrencyData {
    private String name;
    private String shorty;
    private double rate;

    public CurrencyData(String name, double rate, String shorty){
        this.name = name;
        this.rate = rate;
        this.shorty = shorty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShorty() {
        return shorty;
    }

    public void setShorty(String shorty) {
        this.shorty = shorty;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
