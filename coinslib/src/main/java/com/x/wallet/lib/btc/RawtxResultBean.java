package com.x.wallet.lib.btc;

import java.util.List;

/**
 * Created by wuliang on 18-4-12.
 */

public class RawtxResultBean {
    private Data data;

    public Data getData() {
        return data;
    }

    class Data{
        private String txid;
        private int block_no;
        private List<InputBean> inputs;

        public String getTxid() {
            return txid;
        }

        public int getBlock_no() {
            return block_no;
        }

        public List<InputBean> getInputs() {
            return inputs;
        }
    }

    class InputBean{
        private String script_hex;
        private ReceivedFromBean received_from;

        public String getScript_hex() {
            return script_hex;
        }

        public ReceivedFromBean getReceived_from() {
            return received_from;
        }
    }

    class ReceivedFromBean{
        private String txid;

        public String getTxid() {
            return txid;
        }
    }
}
