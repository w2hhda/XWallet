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

import android.database.sqlite.SQLiteOpenHelper;

import com.x.wallet.XWalletApplication;
import com.x.wallet.db.WalletDatabaseHelper;

import net.bither.bitherj.db.imp.AbstractBlockProvider;
import net.bither.bitherj.db.imp.base.IDb;

public class BlockProvider extends AbstractBlockProvider {
    private static BlockProvider blockProvider = new BlockProvider(WalletDatabaseHelper.getInstance(XWalletApplication.getApplication()));

    public static BlockProvider getInstance() {
        return blockProvider;
    }

    private SQLiteOpenHelper helper;

    public BlockProvider(SQLiteOpenHelper helper) {
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
}
