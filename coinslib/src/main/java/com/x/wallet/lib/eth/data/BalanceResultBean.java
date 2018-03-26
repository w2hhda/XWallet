package com.x.wallet.lib.eth.data;

import java.util.List;

/**
 * Created by wuliang on 18-3-26.
 */
//response = {"status":"1","message":"OK","result":[{"account":"0xac4d55b63d29038ac19f2efce8a7bfaf1c3f0beb","balance":"100000000000000"}]}
public class BalanceResultBean {
    private int status;
    private String message;
    private List<ResultBean> result;

    public class ResultBean{
        private String account ;
        private String balance;

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }
}
