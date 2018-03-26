package com.x.wallet.lib.eth.data;

import java.util.List;

/**
 * Created by wuliang on 18-3-26.
 */
// {"status":"1","message":"OK","result":{"ethbtc":"0.06011","ethbtc_timestamp":"1522063191","ethusd":"486.2","ethusd_timestamp":"1522063191"}}
public class PriceResultBean {
    private int status;
    private String message;
    private PriceResultBean.ResultBean result;

    public class ResultBean{
        private double ethbtc;
        private long ethbtc_timestamp;
        private double ethusd;
        private long ethusd_timestamp;

        public double getEthusd() {
            return ethusd;
        }
    }

    public ResultBean getResult() {
        return result;
    }
}
