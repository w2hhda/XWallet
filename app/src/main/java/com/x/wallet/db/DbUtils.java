package com.x.wallet.db;

/**
 * Created by wuliang on 18-3-14.
 */

public class DbUtils {
    public interface DbColumns{
        String _ID = "_id";
        String ADDRESS = "address";
        String NAME = "name";
        String COIN_NAME = "coin_name";
        String ENCRYPT_SEED = "encrypt_seed";
        String ENCRYPT_MNEMONIC = "encrypt_mnemonic";
    }
}