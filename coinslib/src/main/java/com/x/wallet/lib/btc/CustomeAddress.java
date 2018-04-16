package com.x.wallet.lib.btc;

import net.bither.bitherj.core.Coin;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.core.TxBuilder;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.TransactionSignature;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.exception.PasswordException;
import net.bither.bitherj.exception.TxBuilderException;
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
    public static boolean initTxs2(List<Tx> txs) {
        AbstractDb.txProvider.addTxs(txs);
        //notificatTx(null, Tx.TxNotificationType.txFromApi);
        return true;
    }

    public static TxBuildResult buildTx2(long amount, String fromAddress, String toAddress, String changeAddress) throws TxBuilderException {
        List<Long> amounts = new ArrayList<Long>();
        amounts.add(amount);
        List<String> addresses = new ArrayList<String>();
        addresses.add(toAddress);
        return buildTx2(fromAddress, changeAddress, amounts, addresses);
    }

    public static TxBuildResult buildTx2(String fromAddress, String changeAddress, List<Long> amounts, List<String> addresses) throws TxBuilderException {
        return TxBuilder.getInstance().buildTx(fromAddress, changeAddress, amounts, addresses, Coin.BTC);
    }

    public static void signTx2(String address, Tx tx, String passphrase) {
        tx.signWithSignatures(signHashes2(address, tx.getUnsignedInHashes(), passphrase, TransactionSignature.SigHash.ALL));
    }

    public static List<byte[]> signHashes2(String address, List<byte[]> unsignedInHashes, CharSequence passphrase, TransactionSignature.SigHash sigHash) throws
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
