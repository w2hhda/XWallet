package com.x.wallet.lib.btc;

import java.util.List;

/**
 * Created by wuliang on 18-4-12.
 */

public class RawaddrResultBean {
    private String hash160;
    private String address;
    private String n_tx;
    private List<Tx> txs;

    public String getHash160() {
        return hash160;
    }

    public String getAddress() {
        return address;
    }

    public String getN_tx() {
        return n_tx;
    }

    public List<Tx> getTxs() {
        return txs;
    }

    public class Tx{
        private int ver;
        private List<Input> inputs;
        private int block_height;
        private List<Out> out;
        private long lock_time;
        private int time;
        private String hash;

        public int getVer() {
            return ver;
        }

        public List<Input> getInputs() {
            return inputs;
        }

        public int getBlock_height() {
            return block_height;
        }

        public List<Out> getOut() {
            return out;
        }

        public long getLock_time() {
            return lock_time;
        }

        public int getTime() {
            return time;
        }

        public String getHash() {
            return hash;
        }
    }

    public class Input{
        private PrivOut prev_out;
        private String script;
        private long sequence;

        public PrivOut getPrev_out() {
            return prev_out;
        }

        public String getScript() {
            return script;
        }

        public long getSequence() {
            return sequence;
        }
    }

    public class PrivOut{
        private int n;
        private String script;

        public int getN() {
            return n;
        }

        public String getScript() {
            return script;
        }
    }

    public class Out{
        private String addr;
        private long value;
        private int n;
        private String script;

        public String getAddr() {
            return addr;
        }

        public long getValue() {
            return value;
        }

        public int getN() {
            return n;
        }

        public String getScript() {
            return script;
        }
    }
}
