package com.xwallet.ethwallet.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Created by zhangxing on 18-3-13.
 */
public class EthAccount {
    private static EthAccount instance;

    private EthAccount(){

    }

    public static EthAccount getInstance() {
        if (instance == null){
            synchronized (EthAccount.class){
                if (instance == null){
                    instance = new EthAccount();
                }
            }
        }
        return instance;
    }

    public static String generateNewWalletFile(String password, File destinationDirectory, Boolean useFullScript)
            throws NoSuchAlgorithmException, NoSuchProviderException
            , CipherException, IOException, InvalidAlgorithmParameterException{
        ECKeyPair ecKeypair = Keys.createEcKeyPair();
        return  generateWalletFile(password, ecKeypair, destinationDirectory, useFullScript);
    }

    public static String generateWalletFile(String password, ECKeyPair ecKeyPair, File file, Boolean useFullScript)
        throws CipherException, IOException{
        WalletFile walletFile;
        if (useFullScript){
            walletFile = Wallet.createStandard(password, ecKeyPair);
        }else {
            walletFile = Wallet.createLight(password, ecKeyPair);
        }
        String fileName = getWalletFileName(walletFile);
        File desFile = new File(file, fileName);
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        objectMapper.writeValue(desFile, walletFile);
        return  fileName;
    }

    private static String getWalletFileName(WalletFile walletFile){
        return walletFile.getAddress();
    }

    public static Boolean issValidPrivateKey(String privateKey){
        return WalletUtils.isValidPrivateKey(privateKey);
    }

}