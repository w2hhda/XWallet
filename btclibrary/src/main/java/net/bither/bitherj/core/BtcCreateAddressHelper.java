package net.bither.bitherj.core;

import android.util.Log;

import com.x.wallet.btclibrary.AccountData;

import net.bither.bitherj.crypto.EncryptedData;
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

    public static AccountData createAddressFromRandom(SecureRandom random, CharSequence password)
            throws MnemonicException.MnemonicLengthException {
        byte[] mnemonicSeed = new byte[16];
        random.nextBytes(mnemonicSeed);

        return create(mnemonicSeed, password);
    }

    public static AccountData createAddressFromImportMnemonic(List<String> words, CharSequence password){
        try{
            byte[] mnemonicSeed = MnemonicHelper.toEntropy(words);
            return create(mnemonicSeed, password);
        } catch (Exception e){
            Log.e("BtcCreateAddressHelper", "createAddressFromImportMnemonic has a exception!", e);
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
                encryptedMnemonicSeed.toEncryptedString());
        externalKey.wipe();
        return accountData;
    }

    private static DeterministicKey getChainRootKey(DeterministicKey accountKey, AbstractHD.PathType
            pathType) {
        return accountKey.deriveSoftened(pathType.getValue());
    }
}
