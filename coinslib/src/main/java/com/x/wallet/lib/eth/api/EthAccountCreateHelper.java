package com.x.wallet.lib.eth.api;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.x.wallet.lib.common.AccountData;
import com.x.wallet.lib.common.LibUtils;

import net.bither.bitherj.core.AbstractHD;
import net.bither.bitherj.crypto.EncryptedData;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.hd.HDKeyDerivation;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;
import net.bither.bitherj.crypto.mnemonic.MnemonicHelper;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

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

            String address = "0x" + walletFile.getAddress();
            return new AccountData(address, null,
                    encryptedMnemonic.toEncryptedString(), null,
                    keyStore);
        } catch (Exception e){
            Log.i(LibUtils.TAG_ETH, "EthAccountCreateHelper startToGenerateAccount exception", e);
        }
        return null;
    }

    public static AccountData importFromPrivateKey(String privateKey, String password){
        try {
            //Credentials credentials = WalletUtils.loadCredentials(password, privateKey);
            BigInteger key = new BigInteger(privateKey, 16);
            ECKeyPair keyPair = ECKeyPair.create(key);
            WalletFile walletFile = Wallet.createStandard(password, keyPair);
            return  new AccountData(walletFile.getAddress(), null, null, null,privateKey);
        } catch (CipherException e){

        }

        return null;

    }

    public static AccountData importFromKeyStore(String keyStore, String password){
        try{
            ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
            WalletFile file = mapper.readValue(keyStore, WalletFile.class);
            if (file != null){
                ECKeyPair pair = Wallet.decrypt(password, file);
                return new AccountData(file.getAddress(), null, null, pair.getPrivateKey().toString(), keyStore);
            }
        }catch (CipherException e){

        }catch (IOException e){

        }
        return null;
    }

    public static AccountData importFromMnemonic(List<String> mnemonicWords, String password){
        try {
            byte[] mnemonicSeed = MnemonicHelper.toEntropy(mnemonicWords);
            return create(mnemonicSeed, password);
        } catch (MnemonicException.MnemonicLengthException e) {
            e.printStackTrace();
        } catch (MnemonicException.MnemonicWordException e) {
            e.printStackTrace();
        } catch (MnemonicException.MnemonicChecksumException e) {
            e.printStackTrace();
        }

        return null;
    }
}
