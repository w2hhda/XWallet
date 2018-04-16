package com.x.wallet.lib.eth.data;

import java.util.List;

public class TokenResultBean {
    private int status;
    private String message;
    private List<ResultBean> result;

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

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    //    {
//        "blockHash":"0x2e853b5e8c9732744788903703879ea62f7ee7ed3e03ba80efe7ea3efcfd1da4",
//        "blockNumber":"5368476",
//        "confirmations":"65642",
//        "contractAddress":"0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0",
//        "cumulativeGasUsed":"2667151",
//        "from":"0xe2258d66b820fc4f0017017373c7b9f742596f27",
//        "gas":"91000",
//        "gasPrice":"1300000000",
//        "gasUsed":"40715",
//        "hash":"0x9013dc3ec053dc03f1eeba87692f603c350d07e1cb6970518976b30c2338b92f",
//        "input":"0xa9059cbb000000000000000000000000bf66eb7d5c587ef111c6ab8191080322582c61ab000000000000000000000000000000000000000000000000002386f26fc10000",
//        "nonce":"14",
//        "timeStamp":"1522693715",
//        "to":"0xbf66eb7d5c587ef111c6ab8191080322582c61ab",
//        "tokenDecimal":"18",
//        "tokenName":"EOS",
//        "tokenSymbol":"EOS",
//        "transactionIndex":"69",
//        "value":"10000000000000000"
//    }
    public class ResultBean{
        String blockHash;
        String blockNumber;
        String confirmations;
        String contractAddress;
        String cumulativeGasUsed;
        String from;
        String gas;
        String gasPrice;
        String gasUsed;
        String hash;
        String input;
        String nonce;
        String timeStamp;
        String to;
        String tokenDecimal;
        String tokenName;
        String tokenSymbol;
        String transactionIndex;
        String value;

        public String getBlockHash() {
            return blockHash;
        }

        public void setBlockHash(String blockHash) {
            this.blockHash = blockHash;
        }
        public String getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(String blockNumber) {
            this.blockNumber = blockNumber;
        }

        public String getConfirmations() {
            return confirmations;
        }

        public void setConfirmations(String confirmations) {
            this.confirmations = confirmations;
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

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
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

        public String getGasUsed() {
            return gasUsed;
        }

        public void setGasUsed(String gasUsed) {
            this.gasUsed = gasUsed;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getTokenDecimal() {
            return tokenDecimal;
        }

        public void setTokenDecimal(String tokenDecimal) {
            this.tokenDecimal = tokenDecimal;
        }

        public String getTokenName() {
            return tokenName;
        }

        public void setTokenName(String tokenName) {
            this.tokenName = tokenName;
        }

        public String getTokenSymbol() {
            return tokenSymbol;
        }

        public void setTokenSymbol(String tokenSymbol) {
            this.tokenSymbol = tokenSymbol;
        }

        public String getTransactionIndex() {
            return transactionIndex;
        }

        public void setTransactionIndex(String transactionIndex) {
            this.transactionIndex = transactionIndex;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "ResultBean{" +
                    "blockHash='" + blockHash + '\'' +
                    ", confirmations='" + confirmations + '\'' +
                    ", contractAddress='" + contractAddress + '\'' +
                    ", cumulativeGasUsed='" + cumulativeGasUsed + '\'' +
                    ", from='" + from + '\'' +
                    ", gas='" + gas + '\'' +
                    ", gasPrice='" + gasPrice + '\'' +
                    ", gasUsed='" + gasUsed + '\'' +
                    ", hash='" + hash + '\'' +
                    ", input='" + input + '\'' +
                    ", nonce='" + nonce + '\'' +
                    ", timeStamp='" + timeStamp + '\'' +
                    ", to='" + to + '\'' +
                    ", tokenDecimal='" + tokenDecimal + '\'' +
                    ", tokenName='" + tokenName + '\'' +
                    ", tokenSymbol='" + tokenSymbol + '\'' +
                    ", transactionIndex='" + transactionIndex + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}
