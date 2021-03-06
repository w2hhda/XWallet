/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.x.wallet.btc;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.x.wallet.XWalletApplication;
import com.x.wallet.db.DbUtils;
import com.x.wallet.db.WalletDatabaseHelper;
import com.x.wallet.db.XWalletProvider;
import com.x.wallet.lib.btc.BtcLibHelper;

import net.bither.bitherj.core.In;
import net.bither.bitherj.core.Out;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.imp.AbstractTxProvider;
import net.bither.bitherj.db.imp.base.IDb;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Utils;

public class TxProvider extends AbstractTxProvider {

    private static TxProvider txProvider = new TxProvider(WalletDatabaseHelper.getInstance(XWalletApplication.getApplication()));

    public static TxProvider getInstance() {
        return txProvider;
    }

    private SQLiteOpenHelper helper;

    public TxProvider(SQLiteOpenHelper helper) {
        this.helper = helper;
    }

    @Override
    public IDb getReadDb() {
        return new AndroidDb(this.helper.getReadableDatabase());
    }

    @Override
    public IDb getWriteDb() {
        return new AndroidDb(this.helper.getWritableDatabase());
    }

    @Override
    protected void insertTxToDb(IDb db, Tx tx) {
        //Log.i("testBtcTx", "TxProvider insertTxToDb tx = " + tx);
        AndroidDb mdb = (AndroidDb)db;
        ContentValues cv = new ContentValues();
        if (tx.getBlockNo() != Tx.TX_UNCONFIRMED) {
            cv.put(AbstractDb.TxsColumns.BLOCK_NO, tx.getBlockNo());
        } else {
            cv.putNull(AbstractDb.TxsColumns.BLOCK_NO);
        }
        cv.put(AbstractDb.TxsColumns.TX_HASH, Base58.encode(tx.getTxHash()));
        cv.put(AbstractDb.TxsColumns.SOURCE, tx.getSource());
        cv.put(AbstractDb.TxsColumns.TX_TIME, tx.getTxTime());
        cv.put(AbstractDb.TxsColumns.TX_VER, tx.getTxVer());
        cv.put(AbstractDb.TxsColumns.TX_LOCKTIME, tx.getTxLockTime());
        mdb.getSQLiteDatabase().insert(AbstractDb.Tables.TXS, null, cv);
    }

    @Override
    protected void insertInToDb(IDb db, In in) {
        //Log.i("testBtcTx", "TxProvider insertInToDb ");
        AndroidDb mdb = (AndroidDb)db;
        ContentValues cv = new ContentValues();
        cv.put(AbstractDb.InsColumns.TX_HASH, Base58.encode(in.getTxHash()));
        cv.put(AbstractDb.InsColumns.IN_SN, in.getInSn());
        cv.put(AbstractDb.InsColumns.PREV_TX_HASH, Base58.encode(in.getPrevTxHash()));
        cv.put(AbstractDb.InsColumns.PREV_OUT_SN, in.getPrevOutSn());
        if (in.getInSignature() != null) {
            cv.put(AbstractDb.InsColumns.IN_SIGNATURE, Base58.encode(in.getInSignature()));
        } else {
            cv.putNull(AbstractDb.InsColumns.IN_SIGNATURE);
        }
        cv.put(AbstractDb.InsColumns.IN_SEQUENCE, in.getInSequence());
        mdb.getSQLiteDatabase().insert(AbstractDb.Tables.INS, null, cv);
    }

    @Override
    protected void insertOutToDb(IDb db, Out out) {
        //Log.i("testBtcTx", "TxProvider insertOutToDb ");
        AndroidDb mdb = (AndroidDb)db;
        ContentValues cv = new ContentValues();
        cv.put(AbstractDb.OutsColumns.TX_HASH, Base58.encode(out.getTxHash()));
        cv.put(AbstractDb.OutsColumns.OUT_SN, out.getOutSn());
        cv.put(AbstractDb.OutsColumns.OUT_SCRIPT, Base58.encode(out.getOutScript()));
        cv.put(AbstractDb.OutsColumns.OUT_VALUE, out.getOutValue());
        cv.put(AbstractDb.OutsColumns.OUT_STATUS, out.getOutStatus().getValue());
        if (!Utils.isEmpty(out.getOutAddress())) {
            cv.put(AbstractDb.OutsColumns.OUT_ADDRESS, out.getOutAddress());
        } else {
            cv.putNull(AbstractDb.OutsColumns.OUT_ADDRESS);
        }
        //support hd
        if (out.getHDAccountId() != -1) {
            cv.put(AbstractDb.OutsColumns.HD_ACCOUNT_ID,
                    out.getHDAccountId());
        } else {
            cv.putNull(AbstractDb.OutsColumns.HD_ACCOUNT_ID);
        }
        mdb.getSQLiteDatabase().insert(AbstractDb.Tables.OUTS, null, cv);
    }

    @Override
    public void updateAccountBalance(String address) {
        long balance = BtcLibHelper.updateBalance(address);
        ContentValues values = new ContentValues(1);
        values.put(DbUtils.DbColumns.BALANCE, Long.toString(balance));
        int count = XWalletApplication.getApplication().getApplicationContext().getContentResolver().update(XWalletProvider.CONTENT_URI,
                values, DbUtils.ADDRESS_SELECTION, new String[]{address});
        Log.i("testTx", "TxProvider updateAccountBalance address = " + address + ", balance = " + balance + ", count = " + count);
    }

    @Override
    public void notifyTxChanged() {
        final ContentResolver cr = XWalletApplication.getApplication().getContentResolver();
        cr.notifyChange(BtcUtils.BTC_CONTENT_URI, null);
    }
}