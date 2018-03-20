package com.xwallet.ethwallet.api;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xwallet.ethwallet.data.EthAccountData;

import net.bither.bitherj.core.AbstractHD;
import net.bither.bitherj.crypto.EncryptedData;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.hd.HDKeyDerivation;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;
import net.bither.bitherj.crypto.mnemonic.MnemonicHelper;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;

import java.security.SecureRandom;

/**
 * Created by zhangxing on 18-3-19.
 */

public class EthAccountCreateHelper {
    public static EthAccountData createAccount(SecureRandom random, CharSequence password)
            throws MnemonicException.MnemonicLengthException{
        byte[] mnemonicSeed = new byte[16];
        random.nextBytes(mnemonicSeed);
        return create(mnemonicSeed, password);
    }

    private static EthAccountData create(byte[] mnemonicSeed, CharSequence password)  throws MnemonicException.MnemonicLengthException{
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

    private static EthAccountData startToGenerateAccount(DeterministicKey  accountKey, EncryptedData encryptedMnemonic, CharSequence password ){
        DeterministicKey externalKey = accountKey.deriveSoftened(AbstractHD.PathType.EXTERNAL_ROOT_PATH.getValue());
        accountKey.wipe();

        ECKeyPair keyPair = ECKeyPair.create(externalKey.deriveSoftened(0).getPrivKey());

        try {
            WalletFile walletFile = Wallet.createStandard(password.toString(), keyPair);

            Log.i("@@@@", "address = " + walletFile.getAddress());
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            String keyStore = objectMapper.writeValueAsString(walletFile);
            return new EthAccountData(walletFile.getAddress(), keyStore, encryptedMnemonic.toEncryptedString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (CipherException e){
            e.printStackTrace();
        }

        return null;
    }
}
