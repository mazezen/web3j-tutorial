package com.github.mazezen;

import okhttp3.OkHttpClient;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/**
 * Contract deployed at address: 0x9561c133dd8580860b6b7e504bc5aa500f0f06a7
 * Token Name: Leaf Token
 * Token Symbol: LEAF
 * Total Supply: 1000000000000000000000000
 * Balance: 1000000000000000000000000
 */
public class DeployLeafToken {
    static void main() throws Exception {
        String network = Config.getNetwork("local");
        OkHttpClient httpClient = new OkHttpClient.Builder()
                                          .connectTimeout(30, TimeUnit.SECONDS)
                                          .readTimeout(120, TimeUnit.SECONDS)
                                          .writeTimeout(30, TimeUnit.SECONDS)
                                          .build();
        Web3j web3j = Web3j.build(new HttpService(network, httpClient));

        // 获取第一个账户（Ganache上自动提供的账户）
        Credentials credentials = Credentials.create("0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d");

        // 部署 Leaf 合约
        BigInteger initialSupply = Convert.toWei("1000000", Convert.Unit.ETHER).toBigInteger(); // 初始化供应量为1000000 LEAF（转换为 wei）

        // 设置 gas
        StaticGasProvider gasProvider = new StaticGasProvider(BigInteger.valueOf(20000000000L), BigInteger.valueOf(5_000_000));


        // 使用 web3j command cli 来与以太坊网络交互或生成合约的 Java 类
        // web3j generate solidity -b Leaf.bin -a Leaf.abi -o ./src/main/java -p com.mazezen.github
        Leaf contract = Leaf.deploy(web3j, credentials, gasProvider, initialSupply).send();

        // 输出合约地址
        System.out.println("Contract deployed at address: " + contract.getContractAddress());

        // 查询代币名称
        String name = contract.name().send();
        System.out.println("Token Name: " + name);

        // 查询代币符号
        String symbol = contract.symbol().send();
        System.out.println("Token Symbol: " + symbol);

        // 查询代币总供应量
        BigInteger totalSupply = contract.totalSupply().send();
        System.out.println("Total Supply: " + totalSupply);

        // 查询账户余额
        BigInteger balance = contract.balanceOf(credentials.getAddress()).send();
        System.out.println("Balance: " + balance);

    }
}
