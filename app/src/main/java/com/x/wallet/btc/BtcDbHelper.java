package com.x.wallet.btc;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.x.wallet.XWalletApplication;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.common.LibUtils;

import net.bither.bitherj.db.AbstractDb;

/**
 * Created by wuliang on 18-4-8.
 */

public class BtcDbHelper {
    public static final String CREATE_ADDRESSTXS_SQL = "create table if not exists " + Tables.ADDRESSES_TXS + "(" +
            AddressesTxsColumns.ADDRESS + " text not null, " +
            AddressesTxsColumns.TX_HASH + " text not null, " +
            "primary key (address, tx_hash));";

    public static final String CREATE_TXS_SQL = "create table if not exists " + Tables.TXS + "(" +
            TxsColumns.TX_HASH + " text primary key, " +
            TxsColumns.TX_VER + " integer, " +
            TxsColumns.TX_LOCKTIME + " integer, " +
            TxsColumns.TX_TIME + " integer, " +
            TxsColumns.BLOCK_NO + " integer, " +
            TxsColumns.SOURCE + " integer);";

    public static final String CREATE_INS_SQL = "create table if not exists " + Tables.INS + "(" +
            InsColumns.TX_HASH + " text not null, " +
            InsColumns.IN_SN + " integer not null, " +
            InsColumns.PREV_TX_HASH + " text, " +
            InsColumns.PREV_OUT_SN + " integer, " +
            InsColumns.IN_SIGNATURE + " text, " +
            InsColumns.IN_SEQUENCE + " integer, " +
            "primary key (tx_hash, in_sn));";

    public static final String CREATE_OUTS_SQL = "create table if not exists " + Tables.OUTS + "(" +
            OutsColumns.TX_HASH + " text not null, " +
            OutsColumns.OUT_SN + " integer not null, " +
            OutsColumns.OUT_SCRIPT + " text not null, " +
            OutsColumns.OUT_VALUE + " out_value integer not null, " +
            OutsColumns.OUT_STATUS + " integer not null, " +
            OutsColumns.OUT_ADDRESS + " text, " +
            OutsColumns.ACCOUNT_ID + " integer, " +
            " primary key (tx_hash, out_sn));";

    public static void createBitCoinTable(SQLiteDatabase db) {
        createBlocksTable(db);
        createTxsTable(db);
        createAddressTxsTable(db);
        createInsTable(db);
        createOutsTable(db);
        createPeersTable(db);
    }

    private static void createBlocksTable(SQLiteDatabase db) {
        db.execSQL(AbstractDb.CREATE_BLOCKS_SQL);
        db.execSQL(AbstractDb.CREATE_BLOCK_NO_INDEX);
        db.execSQL(AbstractDb.CREATE_BLOCK_PREV_INDEX);
    }

    private static void createTxsTable(SQLiteDatabase db) {
        db.execSQL(AbstractDb.CREATE_TXS_SQL);
        db.execSQL(AbstractDb.CREATE_TX_BLOCK_NO_INDEX);
    }

    private static void createAddressTxsTable(SQLiteDatabase db) {
        db.execSQL(AbstractDb.CREATE_ADDRESSTXS_SQL);
    }

    private static void createInsTable(SQLiteDatabase db) {
        db.execSQL(AbstractDb.CREATE_INS_SQL);
        db.execSQL(AbstractDb.CREATE_IN_PREV_TX_HASH_INDEX);
    }

    private static void createOutsTable(SQLiteDatabase db) {
        db.execSQL(AbstractDb.CREATE_OUTS_SQL);
        db.execSQL(AbstractDb.CREATE_OUT_OUT_ADDRESS_INDEX);
        db.execSQL(AbstractDb.CREATE_OUT_HD_ACCOUNT_ID_INDEX);
    }

    private static void createPeersTable(SQLiteDatabase db) {
        db.execSQL(AbstractDb.CREATE_PEER_SQL);
    }

    public interface Tables{
        String ADDRESSES_TXS = "addresses_txs";
        String TXS = "txs";
        String INS = "ins";
        String OUTS = "outs";
    }

    public interface AddressesTxsColumns{
        String ADDRESS = "address";
        String TX_HASH = "tx_hash";
    }

    public interface TxsColumns{
        String TX_HASH = "tx_hash";
        String TX_VER = "tx_ver";
        String TX_LOCKTIME = "tx_locktime";
        String TX_TIME = "tx_time";
        String BLOCK_NO = "block_no";
        String SOURCE = "source";
    }

    public interface InsColumns{
        String TX_HASH = "tx_hash";
        String IN_SN = "in_sn";
        String PREV_TX_HASH = "prev_tx_hash";
        String PREV_OUT_SN = "prev_out_sn";
        String IN_SIGNATURE = "in_signature";
        String IN_SEQUENCE = "in_sequence";
    }

    public interface OutsColumns{
        String TX_HASH = "tx_hash";
        String OUT_SN = "out_sn";
        String OUT_SCRIPT = "out_script";
        String OUT_VALUE = "out_value";
        String OUT_STATUS = "out_status";
        String OUT_ADDRESS = "out_address";
        String ACCOUNT_ID = "account_id";
    }

    public static Cursor queryAllNotSyncedAddress(){
        return XWalletApplication.getApplication().getApplicationContext().getContentResolver()
                .query(XWalletProvider.CONTENT_URI, new String[]{"_id", "address"}, "is_synced = 0 AND coin_type = " + LibUtils.COINTYPE.COIN_BTC, null, null);
    }
}
