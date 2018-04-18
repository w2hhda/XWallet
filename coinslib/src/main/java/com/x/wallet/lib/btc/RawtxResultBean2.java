package com.x.wallet.lib.btc;

import java.util.List;

/**
 * Created by wuliang on 18-4-12.
 */

public class RawtxResultBean2 {
    private List<Inputs> inputs;

    public List<Inputs> getInputs() {
        return inputs;
    }

    class Inputs{
        private String prev_hash;
        private String script;

        public String getPrev_hash() {
            return prev_hash;
        }

        public String getScript() {
            return script;
        }
    }
}
