package com.github.mazezen;

import com.github.mazezen.config.YamlConfig;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;

public class BlockClass {
    static void main(String[] args) {
        System.out.println(args);
        String local = YamlConfig.getMainNetWorkNode("local");

        Web3j web3j = Web3j.build(new HttpService(local));

        BigInteger nowBlockNumber = nowBlockNumber(web3j);
        System.out.println("最新的区块高度: " + nowBlockNumber);
    }

    /**
     * 获取最新的区块高度
     *
     * @param web3j
     * @return
     */
    public static BigInteger nowBlockNumber(Web3j web3j) {
        try {
            BigInteger nowBlock = web3j.ethBlockNumber().send().getBlockNumber();
            return nowBlock;

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return BigInteger.ZERO;
    }
}

