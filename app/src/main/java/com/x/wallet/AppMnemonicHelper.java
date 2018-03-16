package com.x.wallet;

import net.bither.bitherj.crypto.mnemonic.MnemonicHelper;

import java.io.InputStream;

public class AppMnemonicHelper {

    public static void init(int mnemonicType) {
        MnemonicHelper.initWordList(mnemonicType, getMnemonicWordListRawResource(mnemonicType));
    }

    private static InputStream getMnemonicWordListRawResource(int mnemonicType) {
        switch (mnemonicType) {
            case MnemonicHelper.MNEMONICTYPE.MNEMONICTYPE_EN:
                return XWalletApplication.getApplication().getResources().openRawResource(R.raw.mnemonic_wordlist_english);
            case MnemonicHelper.MNEMONICTYPE.MNEMONICTYPE_ZHCN:
                return XWalletApplication.getApplication().getResources().openRawResource(R.raw.mnemonic_wordlist_zh_cn);
            case MnemonicHelper.MNEMONICTYPE.MNEMONICTYPE_ZHTW:
                return XWalletApplication.getApplication().getResources().openRawResource(R.raw.mnemonic_wordlist_zh_tw);
        }
        return XWalletApplication.getApplication().getResources().openRawResource(R.raw.mnemonic_wordlist_english);
    }
}
