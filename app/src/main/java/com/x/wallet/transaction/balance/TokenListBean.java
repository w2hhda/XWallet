package com.x.wallet.transaction.balance;

import java.util.List;

/**
 * Created by wuliang on 18-3-31.
 */
/*
{"address":"0xbf66eb7d5c587ef111c6ab8191080322582c61ab","ETH":{"balance":0.00130736,"totalIn":0.01040736,"totalOut":0.0091},"countTxs":8,"tokens":[{"tokenInfo":{"address":"0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0","name":"EOS","decimals":18,"symbol":"EOS","totalSupply":"1000000000000000000000000000","owner":"0xd0a6e6c54dbc68db5db3a091b171a77407ff7ccf","lastUpdated":1522467889,"totalIn":2.5964967036019e+27,"totalOut":2.5964967036019e+27,"issuancesCount":0,"holdersCount":302887,"description":"https://eos.io/","price":{"rate":"6.14596","diff":2.58,"diff7d":-11.89,"ts":"1522466951","marketCapUsd":"4683448345.0","availableSupply":"762036906.0","volume24h":"417252000.0","currency":"USD"}},"balance":2.518920064e+20,"totalIn":0,"totalOut":0}]}
*/
public class TokenListBean {
    private String address;
    private EthObject ETH;
    private int countTxs;
    private List<TokenBean> tokens;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<TokenBean> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenBean> tokens) {
        this.tokens = tokens;
    }

    class EthObject{
        double balance;
        double totalIn;
        double totalOut;
    }

    public static class TokenBean{
        private TokenInfo tokenInfo;
        private String balance; //"balance": 251892006400000000000,
        private int totalIn;        //"totalIn": 0,
        private int  totalOut;      //"totalOut": 0

        public TokenBean() {
        }

        public void setTokenInfo(TokenInfo tokenInfo) {
            this.tokenInfo = tokenInfo;
        }

        public TokenInfo getTokenInfo() {
            return tokenInfo;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public String getBalance() {
            return balance;
        }

        @Override
        public String toString() {
            return "TokenBean{" +
                    "tokenInfo=" + tokenInfo +
                    ", balance='" + balance + '\'' +
                    '}';
        }
    }

    public static class TokenInfo{
        private String address;
        private String name;            // "EOS",
        private int decimals;        //"decimals": 18,
        private String symbol;       // "symbol": "EOS",
        private String   totalSupply;  //  "totalSupply": "1000000000000000000000000000",
        private String owner;      //"owner": "0xd0a6e6c54dbc68db5db3a091b171a77407ff7ccf",
        private long lastUpdated;       //"lastUpdated": 1522467889,
        private String totalIn;       //"totalIn": 2.5964967036019e+27,
        private String totalOut;      //"totalOut": 2.5964967036019e+27,
        private String issuancesCount;       //"issuancesCount": 0,
        private String holdersCount;       //"holdersCount": 302887,
        private String description;       //"description": "https://eos.io/",
        private Price price;

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setDecimals(int decimals) {
            this.decimals = decimals;
        }

        public int getDecimals() {
            return decimals;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setPrice(Price price) {
            this.price = price;
        }

        public Price getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return "TokenInfo{" +
                    "address='" + address + '\'' +
                    ", name='" + name + '\'' +
                    ", decimals=" + decimals +
                    ", symbol='" + symbol + '\'' +
                    ", price=" + price +
                    '}';
        }
    }

    public static class Price{
        private double rate;   //"rate": "6.14596",
        private double diff;        //"diff": 2.58,
        private double diff7d;      //"diff7d": -11.89,
        private long  ts;      //"ts": "1522466951",
        private String marketCapUsd;       //"marketCapUsd": "4683448345.0",
        private String availableSupply;       //"availableSupply": "762036906.0",
        private String volume24h;       //"volume24h": "417252000.0",
        private String currency;       //"currency": "USD"

        public Price() {
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

        public double getRate() {
            return rate;
        }

        @Override
        public String toString() {
            return "Price{" +
                    "rate=" + rate +
                    '}';
        }
    }
}
