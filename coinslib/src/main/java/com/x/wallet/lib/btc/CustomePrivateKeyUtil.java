package com.x.wallet.lib.btc;

import net.bither.bitherj.crypto.KeyCrypterScrypt;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.qrcode.SaltForQRCode;
import net.bither.bitherj.utils.Utils;

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
}
