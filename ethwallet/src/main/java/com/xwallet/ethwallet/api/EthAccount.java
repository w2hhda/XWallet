package com.xwallet.ethwallet.api;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xwallet.ethwallet.data.EthAccountData;

import net.bither.bitherj.core.AbstractHD;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.hd.HDKeyDerivation;
import net.bither.bitherj.crypto.mnemonic.MnemonicCode;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;
import net.bither.bitherj.crypto.mnemonic.MnemonicHelper;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * Created by zhangxing on 18-3-13.
 */
public class EthAccount {

    protected Boolean isFromXRandom;
    private EthAccountData ethAccountData;
    protected transient byte[] seed;
    protected transient byte[] mnemonicSeed;
    private MnemonicCode mnemonicCode = MnemonicCode.instance();

    public EthAccount(CharSequence password)
            throws CipherException, InvalidAlgorithmParameterException, NoSuchProviderException,
            NoSuchAlgorithmException, JsonProcessingException, MnemonicException.MnemonicLengthException{
        SecureRandom random = new SecureRandom();
        mnemonicSeed = new byte[16];
        random.nextBytes(mnemonicSeed);

        seed = MnemonicHelper.seedFromMnemonic(mnemonicSeed);
        Log.i("@@@@", "seed = " + seed);
        DeterministicKey key = HDKeyDerivation.createMasterPrivateKey(seed);

        DeterministicKey account = getAccount(key);

        //ECKeyPair ecKeyPair = ECKeyPair.create(account.getPrivKey());
        //WalletFile walletFile = Wallet.createStandard(password.toString(), ecKeyPair);

        DeterministicKey externalKey = account.deriveSoftened(AbstractHD.PathType.EXTERNAL_ROOT_PATH.getValue());
        byte[] externalPub = externalKey.deriveSoftened(0).getPubKey();
        //String address = walletFile.getAddress();
        byte[] address = Keys.getAddress(externalPub);
        Log.i("@@@@", " key = " + externalKey.deriveSoftened(0).getPrivKey().toString(16));



        ECKeyPair keyPair = ECKeyPair.create(externalKey.deriveSoftened(0).getPrivKey());
        WalletFile walletFile = Wallet.createStandard(password.toString(), keyPair);

        Log.i("@@@@", "address = " + walletFile.getAddress());
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        String keyStore = objectMapper.writeValueAsString(walletFile);
        Log.i("@@@@","keyStore = " + keyStore);

        Log.i("@@@@", "code = " + MnemonicHelper.toMnemonic(mnemonicSeed));
        //ethAccountData = new EthAccountData(address.toString(), keyStore);
        //EncryptedData encryptedData

    }

    protected DeterministicKey getAccount(DeterministicKey master) {
        DeterministicKey purpose = master.deriveHardened(44);
        DeterministicKey coinType = purpose.deriveHardened(60);
        DeterministicKey account = coinType.deriveHardened(0);
        purpose.wipe();
        coinType.wipe();
        return account;
    }



    public EthAccountData getEthAccountData(){
        return ethAccountData;
    }

}