package com.github.mazezen;


import okhttp3.OkHttpClient;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * ERC20 Token 代币转账授权操作. 「授权-代理转账」行为（approve + transferFrom 模式）
 * * Approve：代币持有者授权另一个地址（代理/平台）代表自己使用一定数量的代币
 * * TransferFrom：代理地址代表代币持有者发起实际的转账操作，把代币转给第三方
 * -------------------------------------------------------------------------------------------
 * 合约地址: 0x9561c133dd8580860b6b7e504bc5aa500f0f06a7  web3j-deployERC20Contract 部署的合约地址
 * 1. 由部署者 - transfer - sender (100Leaf)
 * 2. sender - approve - platform address (100Leaf)
 * 3. 校验 allowance
 * 4. platform - transferFrom - receiver
 * 5. 解析第四步交易信息
 * -------------------------------------------------------------------------------------------
 * 开始给 sender 转账 100 LEAF
 * 转账交易hash: 0x8ebf31713f4bdd6171ecc6ad872bcc6c0aa77e436f37f20b87e53ed38d5bdfe5
 * sender 当前余额: 100 LEAF
 * Approve tx hash:0xcd0aac355fa114cc09e82b696f7573349c690bb5e26c6395c549e01811e49c00
 * Allowance: 100
 * transferFrom tx hash: 0x2ab296f0742ec7643e146a9ba1f908e47cb1b07764834f77eee22c44f705776e
 * Transfer Event Parsed:
 * From: 0xffcf8fdee72ac11b5c542428b35eef5769c409f0
 * To: 0x22d491bde2303f2f43325b2108d26f1eaba1e32b
 * Amount: 100
 */
public class Main {
    static void main() throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                            .connectTimeout(30, TimeUnit.SECONDS)
                                            .readTimeout(120, TimeUnit.SECONDS)
                                            .writeTimeout(30,TimeUnit.SECONDS)
                                            .build();
        Web3j web3j = Web3j.build(new HttpService("http://192.168.5.134:8545/", okHttpClient));

        // 合约部署者私钥
        Credentials deployerCredentials = Credentials.create("0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d");
        String sender = "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0";
        // Leaf Token 合约地址 web3j-deployERC20Contract 部署的合约地址
        String contractAddress = "0x9561c133dd8580860b6b7e504bc5aa500f0f06a7";

        StaticGasProvider gasProvider = new StaticGasProvider(
                BigInteger.valueOf(20_000_000_000L),
                BigInteger.valueOf(5_000_000));

        Leaf leafContract = Leaf.load(contractAddress, web3j, deployerCredentials, gasProvider);

        // 给 sender 转账 100 LEAF，转换成wei单位
        BigInteger amount = Convert.toWei("100", Convert.Unit.ETHER).toBigInteger();

        System.out.println("开始给 sender 转账 " + Convert.fromWei(amount.toString(), Convert.Unit.ETHER) + " LEAF");
        // 调用 transfer(sender, amount)
        org.web3j.protocol.core.methods.response.TransactionReceipt receiptSender = leafContract.transfer(sender, amount).send();
        System.out.println("转账交易hash: " + receiptSender.getTransactionHash());
        // 查询 sender 余额确认
        BigInteger senderBalance = leafContract.balanceOf(sender).send();
        System.out.println("sender 当前余额: " + Convert.fromWei(senderBalance.toString(), Convert.Unit.ETHER) + " LEAF");


        // Sender
        Credentials credentialsSender = Credentials.create("0x6cbed15c793ce57650b9877cf6fa156fbef513c4e6134f022a85b1ffdd59b2a1");

        // Platform
        Credentials credentialPlatform = Credentials.create("0xb0057716d5917badaf911b193b12b910811c1497b5bada8d7711f758981c3773");

        // receiver
        String receiver = "0x22d491Bde2303f2f43325b2108D26f1eAbA1e32b";

        // sender -> approve Platform (授权100Leaf)
        Leaf leafAsFf = Leaf.load(contractAddress, web3j, credentialsSender, gasProvider);

        BigInteger approveAmount = Convert.toWei("100", Convert.Unit.ETHER).toBigInteger();
        TransactionReceipt approveReceipt = leafAsFf.approve(credentialPlatform.getAddress(), approveAmount).send();

        System.out.println("Approve tx hash:" + approveReceipt.getTransactionHash());

        // 查询 allowance
        BigInteger allowance = leafAsFf.allowance(credentialsSender.getAddress(), credentialPlatform.getAddress()).send();
        System.out.println("Allowance: " + Convert.fromWei(allowance.toString(), Convert.Unit.ETHER));

        // Platform -> transferFrom -> receiver
        Leaf leafAsPlatform = Leaf.load(contractAddress, web3j, credentialPlatform, gasProvider);
        BigInteger transferAmount = Convert.toWei("100", Convert.Unit.ETHER).toBigInteger();
        TransactionReceipt transferReceipt = leafAsPlatform.transferFrom(
                credentialsSender.getAddress(),
                receiver,
                transferAmount
        ).send();

        String txHash = transferReceipt.getTransactionHash();
        System.out.println("transferFrom tx hash: " + txHash);


        // parse transaction
        TransactionReceipt receipt =
                web3j.ethGetTransactionReceipt(txHash)
                        .send()
                        .getTransactionReceipt()
                        .orElseThrow();
        Event transferEvent = new Event("Transfer", Arrays.asList(
                new TypeReference<Address>(true) {},
                new TypeReference<Address>(true) {},
                new TypeReference<Uint256>() {}
        ));

        for (Log log : receipt.getLogs()) {
            EventValues values = Contract.staticExtractEventParameters(transferEvent, log);

            if (values != null) {
                String from = values.getIndexedValues().get(0).getValue().toString();
                String to = values.getIndexedValues().get(1).getValue().toString();
                BigInteger value = (BigInteger) values.getNonIndexedValues().get(0).getValue();

                System.out.println("Transfer Event Parsed:");
                System.out.println("From: " + from);
                System.out.println("To: " + to);
                System.out.println(
                        "Amount: (Leaf)" +
                                Convert.fromWei(value.toString(), Convert.Unit.ETHER)
                );
            }
        }
    }
}
