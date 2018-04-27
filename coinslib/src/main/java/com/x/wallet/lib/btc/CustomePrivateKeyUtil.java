package com.x.wallet.lib.btc;

import android.util.Log;

import com.x.wallet.lib.common.LibUtils;

import net.bither.bitherj.crypto.DumpedPrivateKey;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.EncryptedPrivateKey;
import net.bither.bitherj.crypto.KeyCrypter;
import net.bither.bitherj.crypto.KeyCrypterException;
import net.bither.bitherj.crypto.KeyCrypterScrypt;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.qrcode.SaltForQRCode;
import net.bither.bitherj.utils.Utils;

import org.spongycastle.crypto.params.KeyParameter;

import java.math.BigInteger;

/**
 * Created by wuliang on 18-4-16.
 */

public class CustomePrivateKeyUtil {
    public static String getFullencryptPrivateKey2(String  address, String encryptPrivKey) {
        String[] strings = QRCodeUtil.splitString(encryptPrivKey);
        byte[] salt = Utils.hexStringToByteArray(strings[2]);
        if (salt.length == KeyCrypterScrypt.SALT_LENGTH) {
            SaltForQRCode saltForQRCode = new SaltForQRCode(salt, true, false);
            strings[2] = Utils.bytesToHexString(saltForQRCode.getQrCodeSalt());
        }
        return Utils.joinString(strings, QRCodeUtil.QR_CODE_SPLIT);
    }

    public static ECKey getECKeyFromSingleString(String str, CharSequence password) {
        try {
            DecryptedECKey decryptedECKey = decryptionECKey(str, password, false);
            if (decryptedECKey != null && decryptedECKey.ecKey != null) {
                return decryptedECKey.ecKey;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DecryptedECKey decryptionECKey(String str, CharSequence password, boolean needPrivteKeyText) throws Exception {
        String[] strs = QRCodeUtil.splitOfPasswordSeed(str);
        if (strs.length != 3) {
            Log.w(LibUtils.TAG_BTC,"decryption: PrivateKeyFromString format error");
            return null;
        }
        byte[] temp = Utils.hexStringToByteArray(strs[2]);
        if (temp.length != KeyCrypterScrypt.SALT_LENGTH + 1 && temp.length != KeyCrypterScrypt.SALT_LENGTH) {
            Log.w(LibUtils.TAG_BTC, "decryption:  salt lenth is {" + temp.length + "} not {" + KeyCrypterScrypt.SALT_LENGTH + 1 + "}");
            return null;
        }
        SaltForQRCode saltForQRCode = new SaltForQRCode(temp);
        byte[] salt = saltForQRCode.getSalt();
        boolean isCompressed = saltForQRCode.isCompressed();
        boolean isFromXRandom = saltForQRCode.isFromXRandom();

        KeyCrypterScrypt crypter = new KeyCrypterScrypt(salt);
        EncryptedPrivateKey epk = new EncryptedPrivateKey(Utils.hexStringToByteArray
                (strs[1]), Utils.hexStringToByteArray(strs[0]));
        byte[] decrypted = crypter.decrypt(epk, crypter.deriveKey(password));

        ECKey ecKey = null;
        SecureCharSequence privateKeyText = null;
        if (needPrivteKeyText) {
            DumpedPrivateKey dumpedPrivateKey = new DumpedPrivateKey(decrypted, isCompressed);
            privateKeyText = dumpedPrivateKey.toSecureCharSequence();
            dumpedPrivateKey.clearPrivateKey();
        } else {
            BigInteger bigInteger = new BigInteger(1, decrypted);
            byte[] pub = ECKey.publicKeyFromPrivate(bigInteger, isCompressed);

            ecKey = new ECKey(epk, pub, crypter);
            ecKey.setFromXRandom(isFromXRandom);

        }
        Utils.wipeBytes(decrypted);
        return new DecryptedECKey(ecKey, privateKeyText);
    }

    public static SecureCharSequence getDecryptPrivateKeyString(String str, CharSequence password) {
        try {
            DecryptedECKey decryptedECKey = decryptionECKey(str, password, true);
            if (decryptedECKey != null && decryptedECKey.privateKeyText != null) {
                return decryptedECKey.privateKeyText;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * will release key
     *
     * @param key
     * @param password
     * @return
     */
    public static ECKey encrypt(ECKey key, CharSequence password) {
        KeyCrypter scrypt = new KeyCrypterScrypt();
        KeyParameter derivedKey = scrypt.deriveKey(password);
        ECKey encryptedKey = key.encrypt(scrypt, derivedKey);

        // Check that the encrypted key can be successfully decrypted.
        // This is done as it is a critical failure if the private key cannot be decrypted successfully
        // (all bitcoin controlled by that private key is lost forever).
        // For a correctly constructed keyCrypter the encryption should always be reversible so it is just being as cautious as possible.
        if (!ECKey.encryptionIsReversible(key, encryptedKey, scrypt, derivedKey)) {
            // Abort encryption
            throw new KeyCrypterException("The key " + key.toString() + " cannot be successfully decrypted after encryption so aborting wallet encryption.");
        }
        key.clearPrivateKey();
        return encryptedKey;
    }

    public static String getEncryptedString(ECKey ecKey) {
        String salt = "1";
        if (ecKey.getKeyCrypter() instanceof KeyCrypterScrypt) {
            KeyCrypterScrypt scrypt = (KeyCrypterScrypt) ecKey.getKeyCrypter();
            salt = Utils.bytesToHexString(scrypt.getSalt());
        }
        EncryptedPrivateKey key = ecKey.getEncryptedPrivateKey();
        return Utils.bytesToHexString(key.getEncryptedBytes()) + QRCodeUtil.QR_CODE_SPLIT + Utils
                .bytesToHexString(key.getInitialisationVector()) + QRCodeUtil.QR_CODE_SPLIT + salt;
    }

    private static class DecryptedECKey {
        public DecryptedECKey(ECKey ecKey, SecureCharSequence privateKeyText) {
            this.ecKey = ecKey;
            this.privateKeyText = privateKeyText;
        }

        public ECKey ecKey;
        public SecureCharSequence privateKeyText;
    }
}
