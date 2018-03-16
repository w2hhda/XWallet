package net.bither.bitherj.crypto.mnemonic;

import android.util.Log;

import net.bither.bitherj.utils.Sha256Hash;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliang on 18-3-16.
 */

public class MnemonicHelper {
    private static ArrayList<String> mWordList;
    private static int mMnemonicType = -1;

    public interface MNEMONICTYPE {
        int MNEMONICTYPE_EN = 1;
        int MNEMONICTYPE_ZHCN = 2;
        int MNEMONICTYPE_ZHTW = 3;
    }

    public static void initWordList(int mnemonicType, InputStream inputStream) {
        if (mMnemonicType != mnemonicType) {
            try {
                mWordList = getWordListForInputStream(inputStream);
            } catch (Exception e) {
                Log.e("MnemonicHelper", "initWordList has a exception!", e);
            }
        }
    }

    private static final int PBKDF2_ROUNDS = 2048;

    /**
     * Convert entropy data to mnemonic word list.
     */
    public static List<String> toMnemonic(byte[] entropy) throws MnemonicException
            .MnemonicLengthException {
        if (entropy.length % 4 > 0) {
            throw new MnemonicException.MnemonicLengthException("Entropy length not multiple of "
                    + "32 bits.");
        }

        if (entropy.length == 0) {
            throw new MnemonicException.MnemonicLengthException("Entropy is empty.");
        }

        // We take initial entropy of ENT bits and compute its
        // checksum by taking first ENT / 32 bits of its SHA256 hash.

        byte[] hash = Sha256Hash.create(entropy).getBytes();
        boolean[] hashBits = bytesToBits(hash);

        boolean[] entropyBits = bytesToBits(entropy);
        int checksumLengthBits = entropyBits.length / 32;

        // We append these bits to the end of the initial entropy.
        boolean[] concatBits = new boolean[entropyBits.length + checksumLengthBits];
        System.arraycopy(entropyBits, 0, concatBits, 0, entropyBits.length);
        System.arraycopy(hashBits, 0, concatBits, entropyBits.length, checksumLengthBits);

        // Next we take these concatenated bits and split them into
        // groups of 11 bits. Each group encodes number from 0-2047
        // which is a position in a wordlist.  We convert numbers into
        // words and use joined words as mnemonic sentence.

        ArrayList<String> words = new ArrayList<String>();
        int nwords = concatBits.length / 11;
        for (int i = 0;
             i < nwords;
             ++i) {
            int index = 0;
            for (int j = 0;
                 j < 11;
                 ++j) {
                index <<= 1;
                if (concatBits[(i * 11) + j]) {
                    index |= 0x1;
                }
            }
            words.add(mWordList.get(index));
        }
        /*ArrayList<String> words = new ArrayList<String>();
        String[] t = {"bread", "once", "repeat", "domain", "diesel", "shuffle", "bleak", "raise",
                "tackle", "olive", "stuff", "alien"};
        for(int i = 0; i < 12; i++){
            words.add(t[i]);
        }*/

        return words;
    }

    /**
     * Convert mnemonic word list to seed.
     */
    public static byte[] toSeed(List<String> words, String passphrase) {

        // To create binary seed from mnemonic, we use PBKDF2 function
        // with mnemonic sentence (in UTF-8) used as a password and
        // string "mnemonic" + passphrase (again in UTF-8) used as a
        // salt. Iteration count is set to 4096 and HMAC-SHA512 is
        // used as a pseudo-random function. Desired length of the
        // derived key is 512 bits (= 64 bytes).
        //
        StringBuilder builder = new StringBuilder();
        for (int i = 0;
             i < words.size();
             i++) {
            builder.append(words.get(i));
            if (i < words.size() - 1) {
                builder.append(" ");
            }
        }
        String pass = builder.toString();
        String salt = "mnemonic" + passphrase;

        long start = System.currentTimeMillis();
        byte[] seed = PBKDF2SHA512.derive(pass, salt, PBKDF2_ROUNDS, 64);
        return seed;
    }

    /**
     * Convert mnemonic word list to original entropy value.
     */
    public static byte[] toEntropy(List<String> words) throws MnemonicException.MnemonicLengthException,
            MnemonicException.MnemonicWordException, MnemonicException.MnemonicChecksumException {
        if (words.size() % 3 > 0) {
            throw new MnemonicException.MnemonicLengthException("Word list size must be multiple " +
                    "" + "of three words.");
        }

        if (words.size() == 0) {
            throw new MnemonicException.MnemonicLengthException("Word list is empty.");
        }

        // Look up all the words in the list and construct the
        // concatenation of the original entropy and the checksum.
        //
        int concatLenBits = words.size() * 11;
        boolean[] concatBits = new boolean[concatLenBits];
        int wordindex = 0;
        for (String word : words) {
            // Find the words index in the wordlist.
            int ndx = mWordList.indexOf(word);
            if (ndx < 0) {
                throw new MnemonicException.MnemonicWordException(word);
            }

            // Set the next 11 bits to the value of the index.
            for (int ii = 0;
                 ii < 11;
                 ++ii)
                concatBits[(wordindex * 11) + ii] = (ndx & (1 << (10 - ii))) != 0;
            ++wordindex;
        }

        int checksumLengthBits = concatLenBits / 33;
        int entropyLengthBits = concatLenBits - checksumLengthBits;

        // Extract original entropy as bytes.
        byte[] entropy = new byte[entropyLengthBits / 8];
        for (int ii = 0;
             ii < entropy.length;
             ++ii)
            for (int jj = 0;
                 jj < 8;
                 ++jj)
                if (concatBits[(ii * 8) + jj]) {
                    entropy[ii] |= 1 << (7 - jj);
                }

        // Take the digest of the entropy.
        byte[] hash = Sha256Hash.create(entropy).getBytes();
        boolean[] hashBits = bytesToBits(hash);

        // Check all the checksum bits.
        for (int i = 0;
             i < checksumLengthBits;
             ++i)
            if (concatBits[entropyLengthBits + i] != hashBits[i]) {
                throw new MnemonicException.MnemonicChecksumException();
            }

        return entropy;
    }

    private static boolean[] bytesToBits(byte[] data) {
        boolean[] bits = new boolean[data.length * 8];
        for (int i = 0;
             i < data.length;
             ++i)
            for (int j = 0;
                 j < 8;
                 ++j)
                bits[(i * 8) + j] = (data[i] & (1 << (7 - j))) != 0;
        return bits;
    }

    private static ArrayList<String> getWordListForInputStream(InputStream inputStream) throws IOException, IllegalArgumentException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        ArrayList<String> words = new ArrayList<String>(2048);
        String word;
        while ((word = br.readLine()) != null) {
            words.add(word);
        }
        br.close();

        if (words.size() != 2048) {
            throw new IllegalArgumentException("input stream did not contain 2048 words");
        }
        return words;
    }

    public static byte[] seedFromMnemonic(byte[] mnemonicSeed) throws MnemonicException.MnemonicLengthException {
        return toSeed(toMnemonic(mnemonicSeed), "");
    }
}
