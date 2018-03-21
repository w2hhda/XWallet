package net.bither.bitherj.qrcode;

/**
 * Created by wuliang on 18-3-15.
 */

public class QRCodeUtil {
    public static final String OLD_QR_CODE_SPLIT = ":";
    public static final String QR_CODE_SPLIT = "/";

    public static String[] splitOfPasswordSeed(String str) {
        if (str.indexOf(OLD_QR_CODE_SPLIT) >= 0) {
            return str.split(OLD_QR_CODE_SPLIT);
        } else {
            return str.split(QR_CODE_SPLIT);
        }

    }
}
