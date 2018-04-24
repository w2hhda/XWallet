package com.x.wallet.lib.eth.data;

/**
 * Created by wuliang on 18-3-26.
 */
//response = {"status":"1","message":"OK","result":"580634040000000000"}
public class EthBalanceResultBean {
    private int status;
    private String message;
    private String result;

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getResult() {
        return result;
    }
}
