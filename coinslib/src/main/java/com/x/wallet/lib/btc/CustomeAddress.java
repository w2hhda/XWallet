package com.x.wallet.lib.btc;

import android.util.Log;

import net.bither.bitherj.core.Tx;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.TransactionSignature;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.exception.PasswordException;
import net.bither.bitherj.script.ScriptBuilder;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.bitherj.utils.Utils;

import org.spongycastle.crypto.params.KeyParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliang on 18-4-16.
 */

public class CustomeAddress {
    public static boolean initTxs(List<Tx> txs) {
        AbstractDb.txProvider.addTxs(txs);
        //notificatTx(null, Tx.TxNotificationType.txFromApi);
        return true;
    }

    public static TxBuildResult buildTx(long amount, String fromAddress, String toAddress, String changeAddress, int feeBase) {
        //Log.i("testBtcTx", "CustomeAddress buildTx amount = " + amount );
        //Log.i("testBtcTx", "CustomeAddress buildTx fromAddress = " + fromAddress);
        //Log.i("testBtcTx", "CustomeAddress buildTx toAddress = " + toAddress);
        //Log.i("testBtcTx", "CustomeAddress buildTx changeAddress = " + changeAddress);
        //Log.i("testBtcTx", "CustomeAddress buildTx feeBase = " + feeBase);
        List<Long> amounts = new ArrayList<Long>();
        amounts.add(amount);
        List<String> addresses = new ArrayList<String>();
        addresses.add(toAddress);
        return buildTx(fromAddress, changeAddress, amounts, addresses, feeBase);
    }

    public static TxBuildResult buildTx(String fromAddress, String changeAddress, List<Long> amounts, List<String> addresses, int feeBase){
        return CustomeTxBuilder.buildTx(fromAddress, changeAddress, amounts, addresses, feeBase);
    }

    public static void signTx(String address, Tx tx, String passphrase) {
        tx.signWithSignatures(signHashes(address, tx.getUnsignedInHashes(), passphrase, TransactionSignature.SigHash.ALL));
    }

    public static List<byte[]> signHashes(String address, List<byte[]> unsignedInHashes, CharSequence passphrase, TransactionSignature.SigHash sigHash) throws
            PasswordException {
        ECKey key = PrivateKeyUtil.getECKeyFromSingleString(getFullEncryptPrivKey2(address), passphrase);
        if (key == null) {
            throw new PasswordException("do not decrypt eckey");
        }
        KeyParameter assKey = key.getKeyCrypter().deriveKey(passphrase);
        List<byte[]> result = new ArrayList<byte[]>();
        for (byte[] unsignedInHash : unsignedInHashes) {
            TransactionSignature signature = new TransactionSignature(key.sign(unsignedInHash,
                    assKey), sigHash, false);
            result.add(ScriptBuilder.createInputScript(signature, key).getProgram());
        }
        key.clearPrivateKey();
        return result;
    }

    public static String getFullEncryptPrivKey2(String address) {
        String encryptPrivKeyString = AbstractDb.txProvider.getEncryptPrivateKey(address);
        if (Utils.isEmpty(encryptPrivKeyString)) {
            return "";
        } else {
            return CustomePrivateKeyUtil.getFullencryptPrivateKey2(address
                    , encryptPrivKeyString);
        }
    }
}
