package com.x.wallet.lib.eth.api;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.wallet.lib.common.AccountData;
import com.x.wallet.lib.common.LibUtils;

import net.bither.bitherj.core.AbstractHD;
import net.bither.bitherj.crypto.EncryptedData;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.hd.HDKeyDerivation;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;
import net.bither.bitherj.crypto.mnemonic.MnemonicHelper;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;

import java.security.SecureRandom;

/**
 * Created by zhangxing on 18-3-19.
 */

public class EthAccountCreateHelper {
    public static AccountData createAccount(SecureRandom random, CharSequence password){
        try{
            byte[] mnemonicSeed = new byte[16];
            random.nextBytes(mnemonicSeed);
            return create(mnemonicSeed, password);
        } catch (Exception e){
            Log.e(LibUtils.TAG_ETH, "EthAccountCreateHelper createAccount exception", e);
        }
        return null;
    }

    private static AccountData create(byte[] mnemonicSeed, CharSequence password)  throws MnemonicException.MnemonicLengthException{
        byte[] rootSeed = MnemonicHelper.seedFromMnemonic(mnemonicSeed);
        EncryptedData encryptedMnemonic = new EncryptedData(mnemonicSeed, password);

        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(rootSeed);
        DeterministicKey account = getAccount(master);
        //account.clearPrivateKey();

        return startToGenerateAccount(account, encryptedMnemonic, password);
    }

    private static DeterministicKey getAccount(DeterministicKey master) {
        DeterministicKey purpose = master.deriveHardened(44);
        DeterministicKey coinType = purpose.deriveHardened(60);
        DeterministicKey account = coinType.deriveHardened(0);
        purpose.wipe();
        coinType.wipe();
        return account;
    }

    private static AccountData startToGenerateAccount(DeterministicKey  accountKey, EncryptedData encryptedMnemonic, CharSequence password ){
        DeterministicKey externalKey = accountKey.deriveSoftened(AbstractHD.PathType.EXTERNAL_ROOT_PATH.getValue());
        accountKey.wipe();

        ECKeyPair keyPair = ECKeyPair.create(externalKey.deriveSoftened(0).getPrivKey());

        try {
            WalletFile walletFile = Wallet.createStandard(password.toString(), keyPair);

            Log.i(LibUtils.TAG_ETH, "EthAccountCreateHelper startToGenerateAccount address = " + walletFile.getAddress());
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            String keyStore = objectMapper.writeValueAsString(walletFile);
            return new AccountData(walletFile.getAddress(), null,
                    encryptedMnemonic.toEncryptedString(), null,
                    keyStore);
        } catch (Exception e){
            Log.i(LibUtils.TAG_ETH, "EthAccountCreateHelper startToGenerateAccount exception", e);
        }
        return null;
    }
}
