package com.x.wallet.lib.eth.data;

import java.util.List;

/**
 * Created by Nick on 27/3/2018.
 */

//transactions = {"status":"1",
// "message":"OK",
// "result":
// [{"blockNumber":"5294859","timeStamp":"1521631271","hash":"0x50f181ce8f653aa738b51e0c66f6929935c577abc43e312f184e46a79699b21c","nonce":"111954","blockHash":"0x5a71e93eb6b8aff5cc56164c3af928ffea436bb0d52d621eb791e03ecc1fc888","transactionIndex":"21","from":"0xf726dc178d1a4d9292a8d63f01e0fa0a1235e65c","to":"0xe2258d66b820fc4f0017017373c7b9f742596f27",
// "value":"32212000000000000","gas":"90000","gasPrice":"28000000000","isError":"0","txreceipt_status":"1","input":"0x","contractAddress":"","cumulativeGasUsed":"851266","gasUsed":"21000","confirmations":"34776"},
// {"blockNumber":"5311189","timeStamp":"1521867377","hash":"0xb918e016ef7f0555d2ce673b09f0ad3c14b86306c9c6eb1f60bb15cf3a480059","nonce":"0","blockHash":"0x218a68d05d436c0cadaa099dc8037702eb4194f30b3671fcd2e93538b951a52f","transactionIndex":"43","from":"0xe2258d66b820fc4f0017017373c7b9f742596f27","to":"0xbf66eb7d5c587ef111c6ab8191080322582c61ab",
// "value":"100000000000000","gas":"4712388","gasPrice":"4000000000","isError":"0","txreceipt_status":"1","input":"0x","contractAddress":"","cumulativeGasUsed":"1365031","gasUsed":"21000","confirmations":"18446"},
// {"blockNumber":"5311453","timeStamp":"1521871242","hash":"0x29fcf519a1ed180a452c8bbc58a65a045156943f15fba5bae3c4e229318b12f5","nonce":"1","blockHash":"0x97d1c85a63483ac280ba186ffd45695df7ff50c6109fc8a4a2539a35276f4482","transactionIndex":"45","from":"0xe2258d66b820fc4f0017017373c7b9f742596f27","to":"0xbf66eb7d5c587ef111c6ab8191080322582c61ab",
// "value":"100000000000000","gas":"4712388","gasPrice":"3659701504","isError":"0","txreceipt_status":"1","input":"0x","contractAddress":"","cumulativeGasUsed":"1060019","gasUsed":"21000","confirmations":"18182"}]}

public class TransactionsResultBean {
    private int status;
    private String message;
    private List<ReceiptBean> result;

    public class ReceiptBean{
        @Override
        public String toString() {
            return "ReceiptBean{" +
                    "blockNumber='" + blockNumber + '\'' +
                    ", timeStamp='" + timeStamp + '\'' +
                    ", hash='" + hash + '\'' +
                    ", nonce='" + nonce + '\'' +
                    ", blockHash='" + blockHash + '\'' +
                    ", transactionIndex='" + transactionIndex + '\'' +
                    ", from='" + from + '\'' +
                    ", to='" + to + '\'' +
                    ", value='" + value + '\'' +
                    ", gas='" + gas + '\'' +
                    ", gasPrice='" + gasPrice + '\'' +
                    ", isError=" + isError +
                    ", txreceipt_status=" + txreceipt_status +
                    ", input='" + input + '\'' +
                    ", contractAddress='" + contractAddress + '\'' +
                    ", cumulativeGasUsed='" + cumulativeGasUsed + '\'' +
                    ", gasUsed='" + gasUsed + '\'' +
                    ", confirmations='" + confirmations + '\'' +
                    '}';
        }

        String blockNumber;
        String timeStamp;
        String hash;
        String nonce;
        String blockHash;
        String transactionIndex;
        String from;
        String to;
        String value;
        String gas;
        String gasPrice;
        int isError;
        int txreceipt_status;
        String input;
        String contractAddress;
        String cumulativeGasUsed;
        String gasUsed;
        String confirmations;

        public String getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(String blockNumber) {
            this.blockNumber = blockNumber;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getBlockHash() {
            return blockHash;
        }

        public void setBlockHash(String blockHash) {
            this.blockHash = blockHash;
        }

        public String getTransactionIndex() {
            return transactionIndex;
        }

        public void setTransactionIndex(String transactionIndex) {
            this.transactionIndex = transactionIndex;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getGas() {
            return gas;
        }

        public void setGas(String gas) {
            this.gas = gas;
        }

        public String getGasPrice() {
            return gasPrice;
        }

        public void setGasPrice(String gasPrice) {
            this.gasPrice = gasPrice;
        }

        public int getIsError() {
            return isError;
        }

        public void setIsError(int isError) {
            this.isError = isError;
        }

        public int getTxreceipt_status() {
            return txreceipt_status;
        }

        public void setTxreceipt_status(int txreceipt_status) {
            this.txreceipt_status = txreceipt_status;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getContractAddress() {
            return contractAddress;
        }

        public void setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
        }

        public String getCumulativeGasUsed() {
            return cumulativeGasUsed;
        }

        public void setCumulativeGasUsed(String cumulativeGasUsed) {
            this.cumulativeGasUsed = cumulativeGasUsed;
        }

        public String getGasUsed() {
            return gasUsed;
        }

        public void setGasUsed(String gasUsed) {
            this.gasUsed = gasUsed;
        }

        public String getConfirmations() {
            return confirmations;
        }

        public void setConfirmations(String confirmations) {
            this.confirmations = confirmations;
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ReceiptBean> getResult() {
        return result;
    }

    public void setResult(List<ReceiptBean> result) {
        this.result = result;
    }
}
