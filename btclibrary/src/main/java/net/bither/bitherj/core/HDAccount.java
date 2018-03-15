package net.bither.bitherj.core;

import com.x.wallet.btclibrary.AccountData;

import net.bither.bitherj.crypto.EncryptedData;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.hd.HDKeyDerivation;
import net.bither.bitherj.crypto.mnemonic.MnemonicCode;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;
import net.bither.bitherj.utils.Utils;

import java.security.SecureRandom;

/**
 * Created by wuliang on 18-3-15.
 */

public class HDAccount {
    protected transient byte[] mnemonicSeed; //radom number
    protected transient byte[] hdSeed; //seed
    protected boolean isFromXRandom;
    private AccountData mAccountData;
    private MnemonicCode mnemonicCode = MnemonicCode.instance();

    public HDAccount(SecureRandom random, CharSequence password) throws MnemonicException.MnemonicLengthException {
        mnemonicSeed = new byte[16];
        random.nextBytes(mnemonicSeed);
        hdSeed = seedFromMnemonic(mnemonicCode, mnemonicSeed);
        EncryptedData encryptedHDSeed = new EncryptedData(hdSeed, password, isFromXRandom);
        EncryptedData encryptedMnemonicSeed = new EncryptedData(mnemonicSeed, password,
                isFromXRandom);
        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(hdSeed);
        DeterministicKey account = getAccount(master);
        account.clearPrivateKey();
        initHDAccount(account, encryptedMnemonicSeed, encryptedHDSeed);
    }

    public static final byte[] seedFromMnemonic(MnemonicCode mnemonicCode, byte[] mnemonicSeed) throws MnemonicException
            .MnemonicLengthException {
        return mnemonicCode.toSeed(mnemonicCode.toMnemonic(mnemonicSeed), "");
    }

    protected DeterministicKey getAccount(DeterministicKey master) {
        DeterministicKey purpose = master.deriveHardened(44);
        DeterministicKey coinType = purpose.deriveHardened(0);
        DeterministicKey account = coinType.deriveHardened(0);
        purpose.wipe();
        coinType.wipe();
        return account;
    }

    private void initHDAccount(DeterministicKey accountKey, EncryptedData encryptedMnemonicSeed,
                               EncryptedData encryptedHDSeed) {
        DeterministicKey externalKey = getChainRootKey(accountKey, AbstractHD.PathType
                .EXTERNAL_ROOT_PATH);
        accountKey.wipe();

        byte[] subExternalPub = externalKey.deriveSoftened(0).getPubKey();
        mAccountData = new AccountData(Utils.toAddress(Utils.sha256hash160(subExternalPub)),
                encryptedHDSeed.toEncryptedString(),
                encryptedMnemonicSeed.toEncryptedString());
        externalKey.wipe();
    }

    protected DeterministicKey getChainRootKey(DeterministicKey accountKey, AbstractHD.PathType
            pathType) {
        return accountKey.deriveSoftened(pathType.getValue());
    }

    public AccountData getAccountData() {
        return mAccountData;
    }
}
