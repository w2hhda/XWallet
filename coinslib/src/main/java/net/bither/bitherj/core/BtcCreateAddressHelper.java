package net.bither.bitherj.core;

import android.text.TextUtils;
import android.util.Log;


import com.x.wallet.lib.common.AccountData;
import com.x.wallet.lib.common.LibUtils;

import net.bither.bitherj.crypto.DumpedPrivateKey;
import net.bither.bitherj.crypto.EncryptedData;
import net.bither.bitherj.crypto.KeyCrypterException;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.hd.HDKeyDerivation;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;
import net.bither.bitherj.crypto.mnemonic.MnemonicHelper;
import net.bither.bitherj.utils.Utils;

import java.security.SecureRandom;
import java.util.List;

/**
 * Created by wuliang on 18-3-15.
 */

public class BtcCreateAddressHelper {

    public static AccountData createAddressFromRandom(SecureRandom random, CharSequence password){
        try {
            byte[] mnemonicSeed = new byte[16];
            random.nextBytes(mnemonicSeed);

            return create(mnemonicSeed, password);
        } catch (Exception e){
            Log.e(LibUtils.TAG_BTC, "BtcCreateAddressHelper createAddressFromRandom exception", e);
        }
        return null;
    }

    public static AccountData createAddressFromImportMnemonic(List<String> words, CharSequence password){
        try{
            byte[] mnemonicSeed = MnemonicHelper.toEntropy(words);
            return create(mnemonicSeed, password);
        } catch (Exception e){
            Log.e(LibUtils.TAG_BTC, "BtcCreateAddressHelper createAddressFromImportMnemonic exception", e);
        }
        return null;
    }

    private static AccountData create(byte[] mnemonicSeed, CharSequence password) throws MnemonicException.MnemonicLengthException{
        byte[] hdSeed = MnemonicHelper.seedFromMnemonic(mnemonicSeed);

        EncryptedData encryptedHDSeed = new EncryptedData(hdSeed, password, false);
        EncryptedData encryptedMnemonicSeed = new EncryptedData(mnemonicSeed, password, false);

        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(hdSeed);
        DeterministicKey account = getAccount(master);
        account.clearPrivateKey();

        return startToGenerateAddress(account, encryptedMnemonicSeed, encryptedHDSeed);
    }

    private static DeterministicKey getAccount(DeterministicKey master) {
        DeterministicKey purpose = master.deriveHardened(44);
        DeterministicKey coinType = purpose.deriveHardened(0);
        DeterministicKey account = coinType.deriveHardened(0);
        purpose.wipe();
        coinType.wipe();
        return account;
    }

    private static AccountData startToGenerateAddress(DeterministicKey accountKey, EncryptedData encryptedMnemonicSeed,
                                        EncryptedData encryptedHDSeed) {
        DeterministicKey externalKey = getChainRootKey(accountKey, AbstractHD.PathType
                .EXTERNAL_ROOT_PATH);
        accountKey.wipe();

        byte[] subExternalPub = externalKey.deriveSoftened(0).getPubKey();
        AccountData accountData = new AccountData(Utils.toAddress(Utils.sha256hash160(subExternalPub)),
                encryptedHDSeed.toEncryptedString(),
                encryptedMnemonicSeed.toEncryptedString(),
                null, null);
        externalKey.wipe();
        return accountData;
    }

    private static DeterministicKey getChainRootKey(DeterministicKey accountKey, AbstractHD.PathType
            pathType) {
        return accountKey.deriveSoftened(pathType.getValue());
    }

    public static String readPrivateKey(String encryptSeed, String password){
        try {
            byte[] decryptSeed = decryptHDSeed(encryptSeed, password);

            if(decryptSeed == null){
                Log.e("BtcCreateAddressHelper", "readPrivateKey decrypt failed for decrypting failed!");
                return null;
            }
            DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(decryptSeed);
            wipeByteShuzu(decryptSeed);

            if(master == null){
                Log.e("BtcCreateAddressHelper", "readPrivateKey decrypt failed for master is null!");
                return null;
            }

            DeterministicKey account = getAccount(master);
            DeterministicKey externalKey = getChainRootKey(account, AbstractHD.PathType.EXTERNAL_ROOT_PATH);
            account.wipe();
            master.wipe();

            DeterministicKey key = externalKey.deriveSoftened(0);
            if(key != null){
                final SecureCharSequence privateKey = new DumpedPrivateKey(key.getPrivKeyBytes(), true).toSecureCharSequence();
                return privateKey != null ? privateKey.toString() : null;
            }
        } catch (Exception e){
            Log.e("BtcCreateAddressHelper", "readPrivateKey", e);
        }
        return null;
    }

    public static String readMnemonic(String encryptMnemonic, String password){
        byte[] mnemonicSeed = null;
        try {
            mnemonicSeed = decryptMnemonicSeed(encryptMnemonic, password);
            if(mnemonicSeed == null){
                Log.e("BtcCreateAddressHelper", "readMnemonic decrypt failed for decrypting failed!");
                return null;
            }
            List<String> words = MnemonicHelper.toMnemonic(mnemonicSeed);
            StringBuilder builder = new StringBuilder();
            for(String word: words){
                builder.append(" ");
                builder.append(word);
            }
            return builder.toString().substring(1);
        } catch (Exception e){
            Log.e("BtcCreateAddressHelper", "readMnemonic decrypt failed for exception!", e);
        } finally {
            wipeByteShuzu(mnemonicSeed);
        }
        return null;
    }

    public static List<String> readMnemonicToList(String encryptMnemonic, String password){
        byte[] mnemonicSeed = null;
        try {
            mnemonicSeed = decryptMnemonicSeed(encryptMnemonic, password);
            if(mnemonicSeed == null){
                Log.e("BtcCreateAddressHelper", "readMnemonicToList decrypt failed for decrypting failed!");
                return null;
            }
            List<String> words = MnemonicHelper.toMnemonic(mnemonicSeed);
            return words;
        } catch (Exception e){
            Log.e("BtcCreateAddressHelper", "readMnemonicToList decrypt failed for exception!", e);
        } finally {
            wipeByteShuzu(mnemonicSeed);
        }
        return null;
    }

    private static byte[] decryptHDSeed(String encryptSeed, CharSequence password) throws MnemonicException.MnemonicLengthException {
        if (password == null || TextUtils.isEmpty(encryptSeed)) {
            return null;
        }
        return new EncryptedData(encryptSeed).decrypt(password);
    }

    private static byte[] decryptMnemonicSeed(String encryptMnemonic, CharSequence password) throws KeyCrypterException {
        if (password == null || TextUtils.isEmpty(encryptMnemonic)) {
            return null;
        }
        return new EncryptedData(encryptMnemonic).decrypt(password);
    }

    private static void wipeByteShuzu(byte[] value) {
        if (value == null) {
            return;
        }
        Utils.wipeBytes(value);
    }
}
