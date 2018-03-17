package net.bither.bitherj.utils;

import com.google.common.base.Charsets;
import com.google.common.primitives.UnsignedLongs;

import net.bither.bitherj.crypto.SecureCharSequence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Locale;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.util.encoders.Hex;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;
/**
 * Created by wuliang on 18-3-15.
 */

public class Utils {
    private static final MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Can't happen.
        }
    }

    /**
     * Calculates RIPEMD160(SHA256(input)). This is used in Address calculations.
     */
    public static byte[] sha256hash160(byte[] input) {
        try {
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(input);
            RIPEMD160Digest digest = new RIPEMD160Digest();
            digest.update(sha256, 0, sha256.length);
            byte[] out = new byte[20];
            digest.doFinal(out, 0);
            return out;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    public static String toAddress(byte[] pubKeyHash) {
        checkArgument(pubKeyHash.length == 20, "Addresses are 160-bit hashes, " +
                "so you must provide 20 bytes");

        int version = 0;
        checkArgument(version < 256 && version >= 0);

        byte[] addressBytes = new byte[1 + pubKeyHash.length + 4];
        addressBytes[0] = (byte) version;
        System.arraycopy(pubKeyHash, 0, addressBytes, 1, pubKeyHash.length);
        byte[] check = Utils.doubleDigest(addressBytes, 0, pubKeyHash.length + 1);
        System.arraycopy(check, 0, addressBytes, pubKeyHash.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Returns the given byte array hex encoded.
     */
    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars).toUpperCase(Locale.US);
    }

    /**
     * See {@link Utils#doubleDigest(byte[], int, int)}.
     */
    public static byte[] doubleDigest(byte[] input) {
        return doubleDigest(input, 0, input.length);
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash
     * again. This is
     * standard procedure in Bitcoin. The resulting hash is in big endian form.
     */
    public static byte[] doubleDigest(byte[] input, int offset, int length) {
        synchronized (digest) {
            digest.reset();
            digest.update(input, offset, length);
            byte[] first = digest.digest();
            return digest.digest(first);
        }
    }

    public static char[] charsFromBytes(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0;
             i < bytes.length;
             i++) {
            chars[i] = (char) bytes[i];
        }
        return chars;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0;
             i < len;
             i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s
                    .charAt(i + 1), 16));
        }
        return data;
    }

    public static void wipeBytes(byte[] bytes) {
        if (bytes == null) {
            return;
        }
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0;
        }
        SecureRandom r = new SecureRandom();
        r.nextBytes(bytes);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0;
        }
    }

    /**
     * If non-null, overrides the return value of now().
     */
    public static volatile Date mockTime;

    public static long currentTimeSeconds() {
        return currentTimeMillis() / 1000;
    }

    /**
     * Returns the current time in seconds since the epoch, or a mocked out equivalent.
     */
    public static long currentTimeMillis() {
        if (mockTime != null) {
            return mockTime.getTime();
        } else {
            return System.currentTimeMillis();
        }
    }

    /**
     * The string that prefixes all text messages signed using Bitcoin keys.
     */
    public static final String BITCOIN_SIGNED_MESSAGE_HEADER = "Bitcoin Signed Message:\n";
    public static final byte[] BITCOIN_SIGNED_MESSAGE_HEADER_BYTES =
            BITCOIN_SIGNED_MESSAGE_HEADER.getBytes(Charsets.UTF_8);

    /**
     * <p>Given a textual message, returns a byte buffer formatted as follows:</p>
     * <p/>
     * <tt><p>[24] "Bitcoin Signed Message:\n" [message.length as a varint] message</p></tt>
     */
    public static byte[] formatMessageForSigning(String message) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES.length);
            bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES);
            byte[] messageBytes = message.getBytes(Charsets.UTF_8);
            VarInt size = new VarInt(messageBytes.length);
            bos.write(size.encode());
            bos.write(messageBytes);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /**
     * Work around lack of unsigned types in Java.
     */
    public static boolean isLessThanUnsigned(long n1, long n2) {
        return UnsignedLongs.compare(n1, n2) < 0;
    }

    public static long readUint32(byte[] bytes, int offset) {
        return ((bytes[offset++] & 0xFFL)) |
                ((bytes[offset++] & 0xFFL) << 8) |
                ((bytes[offset++] & 0xFFL) << 16) |
                ((bytes[offset] & 0xFFL) << 24);
    }

    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & (val));
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }

    /**
     * The regular {@link java.math.BigInteger#toByteArray()} method isn't quite what we often
     * need: it appends a
     * leading zero to indicate that the number is positive and may need padding.
     *
     * @param b        the integer to format into a byte array
     * @param numBytes the desired size of the resulting byte array
     * @return numBytes byte long array.
     */
    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        if (b == null) {
            return null;
        }
        byte[] bytes = new byte[numBytes];
        byte[] biBytes = b.toByteArray();
        int start = (biBytes.length == numBytes + 1) ? 1 : 0;
        int length = Math.min(biBytes.length, numBytes);
        System.arraycopy(biBytes, start, bytes, numBytes - length, length);
        return bytes;
    }

    // remeber to wipe #address
    public static SecureCharSequence formatHashFromCharSequence(@Nonnull final SecureCharSequence address, final int groupSize, final int lineSize) {
        int length = address.length();
        if (length % groupSize == 0) {
            length = length + length / groupSize - 1;
        } else {
            length = length + length / groupSize;
        }
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            if (i % (groupSize + 1) == groupSize) {
                if ((i + 1) % (lineSize + lineSize / groupSize) == 0) {
                    chars[i] = '\n';
                } else {
                    chars[i] = ' ';
                }
            } else {
                chars[i] = address.charAt(i - i / (groupSize + 1));
            }
        }
        return new SecureCharSequence(chars);
    }
}
