package net.bither.bitherj.core;

import android.text.TextUtils;
import android.util.Log;

import com.x.wallet.lib.btc.CustomePrivateKeyUtil;
import com.x.wallet.lib.common.AccountData;
import com.x.wallet.lib.common.LibUtils;

import net.bither.bitherj.crypto.DumpedPrivateKey;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.EncryptedData;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.hd.HDKeyDerivation;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;
import net.bither.bitherj.crypto.mnemonic.MnemonicHelper;
import net.bither.bitherj.utils.Base58;
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

            return create(false, mnemonicSeed, password);
        } catch (Exception e){
            Log.e(LibUtils.TAG_BTC, "BtcCreateAddressHelper createAddressFromRandom exception", e);
        }
        return null;
    }

    public static AccountData createAddressFromImportMnemonic(List<String> words, CharSequence password){
        try{
            byte[] mnemonicSeed = MnemonicHelper.toEntropy(words);
            return create(true, mnemonicSeed, password);
        } catch (Exception e){
            Log.e(LibUtils.TAG_BTC, "BtcCreateAddressHelper createAddressFromImportMnemonic exception", e);
        }
        return null;
    }

    public static AccountData createAddressFromImportKey(String key, String password) {
        if(!Utils.validBitcoinPrivateKey(key)){
            Log.e(LibUtils.TAG_BTC, "BtcCreateAddressHelper createAddressFromImportKey key is not valid!");
            return null;
        }
        final ECKey compressKey = initEcKey(key, true);
        if (compressKey == null) {
            return null;
        }
        return new AccountData(compressKey.toAddress(),
                null,
                null, getEncryptedString(key, password, true),
                Base58.encode(compressKey.getPubKey()), false);
    }

    private static AccountData create(boolean isFromImport, byte[] mnemonicSeed, CharSequence password) throws MnemonicException.MnemonicLengthException{
        byte[] hdSeed = MnemonicHelper.seedFromMnemonic(mnemonicSeed);

        EncryptedData encryptedHDSeed = new EncryptedData(hdSeed, password, false);
        EncryptedData encryptedMnemonicSeed = new EncryptedData(mnemonicSeed, password, false);

        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(hdSeed);
        DeterministicKey account = getAccount(master);
        return startToGenerateAddress(isFromImport, account, encryptedMnemonicSeed, encryptedHDSeed, password);
    }

    private static DeterministicKey getAccount(DeterministicKey master) {
        DeterministicKey purpose = master.deriveHardened(44);
        DeterministicKey coinType = purpose.deriveHardened(0);
        DeterministicKey account = coinType.deriveHardened(0);
        purpose.wipe();
        coinType.wipe();
        return account;
    }

    private static AccountData startToGenerateAddress(boolean isFromImport, DeterministicKey accountKey, EncryptedData encryptedMnemonicSeed,
                                        EncryptedData encryptedHDSeed, CharSequence password) {
        DeterministicKey externalKey = getChainRootKey(accountKey, AbstractHD.PathType.EXTERNAL_ROOT_PATH);

        DeterministicKey key = externalKey.deriveSoftened(0);
        EncryptedData encryptedPrivKey = new EncryptedData(key.getPrivKeyBytes(), password, false);
        byte[] subExternalPub = key.getPubKey();
        AccountData accountData = new AccountData(Utils.toAddress(Utils.sha256hash160(subExternalPub)),
                encryptedHDSeed.toEncryptedString(),
                encryptedMnemonicSeed.toEncryptedString(),
                encryptedPrivKey.toEncryptedString(), Base58.encode(subExternalPub), !isFromImport);
        accountKey.wipe();
        externalKey.wipe();
        key.wipe();
        return accountData;
    }

    private static DeterministicKey getChainRootKey(DeterministicKey accountKey, AbstractHD.PathType
            pathType) {
        return accountKey.deriveSoftened(pathType.getValue());
    }

    public static String readPrivateKey(String encryptKey, String encryptSeed, String password){
        try {
            if(!TextUtils.isEmpty(encryptKey)){
                SecureCharSequence str = CustomePrivateKeyUtil.getDecryptPrivateKeyString(encryptKey, password);
                String result = str.toString();
                str.wipe();
                return result;
            } else {
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
                    DumpedPrivateKey dumpedPrivateKey = new DumpedPrivateKey(key.getPrivKeyBytes(), true);
                    final SecureCharSequence privateKey = dumpedPrivateKey.toSecureCharSequence();
                    dumpedPrivateKey.clearPrivateKey();
                    key.clearPrivateKey();
                    return privateKey != null ? privateKey.toString() : null;
                }
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

    public static byte[] decryptMnemonicSeed(String encryptMnemonic, CharSequence password){
        try{
            if (password == null || TextUtils.isEmpty(encryptMnemonic)) {
                return null;
            }
            return new EncryptedData(encryptMnemonic).decrypt(password);
        } catch (Exception e){
            Log.e(LibUtils.TAG_BTC, "BtcCreateAddressHelperdecryptMnemonicSeed decrypt failed for exception!", e);
        }
        return null;
    }

    private static void wipeByteShuzu(byte[] value) {
        if (value == null) {
            return;
        }
        Utils.wipeBytes(value);
    }

    public static String encryptMnemonicSeed(byte[] dataToEncrypt, CharSequence password){
        EncryptedData encryptedMnemonicSeed = new EncryptedData(dataToEncrypt, password, false);
        return encryptedMnemonicSeed.toEncryptedString();
    }

    private static ECKey initEcKey(String content, boolean isCompress) {
        ECKey ecKey = getEckey(content);
        ECKey resultKey = null;
        try {
            if (ecKey == null) {
                return null;
            } else {
                resultKey = new ECKey(ecKey.getPriv(), null, isCompress);
                if (resultKey == null) {
                    return null;
                }
                return resultKey;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (ecKey != null) {
                ecKey.clearPrivateKey();
            }
            if (resultKey != null) {
                resultKey.clearPrivateKey();
            }
        }
    }

    private static ECKey getEckey(String content) {
        ECKey ecKey = null;
        DumpedPrivateKey dumpedPrivateKey = null;
        try {
            dumpedPrivateKey = new DumpedPrivateKey(content);
            ecKey = dumpedPrivateKey.getKey();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dumpedPrivateKey != null) {
                dumpedPrivateKey.clearPrivateKey();
            }
        }
        return ecKey;
    }

    private static String getEncryptedString(String content, String password, boolean isCompress){
        ECKey ecKey = getEckey(content);
        ECKey resultKey = null;
        try {
            if (ecKey == null) {
                return null;
            } else {
                resultKey = new ECKey(ecKey.getPriv(), null, isCompress);
                if (resultKey == null) {
                    return null;
                }
                ecKey = CustomePrivateKeyUtil.encrypt(resultKey, password);
                return CustomePrivateKeyUtil.getEncryptedString(ecKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (ecKey != null) {
                ecKey.clearPrivateKey();
            }
            if (resultKey != null) {
                resultKey.clearPrivateKey();
            }
        }
    }
}
