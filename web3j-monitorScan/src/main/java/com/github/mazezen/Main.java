package com.github.mazezen;

import okhttp3.OkHttpClient;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.utils.Convert;

import java.awt.*;
import java.math.BigDecimal;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {
    private static final String WS_URL = "";
    private static final String WATCH_ADDRESS = "".toLowerCase();

    static void main() throws Exception {

        WebSocketClient webSocketClient = new WebSocketClient(new URI(WS_URL));
        WebSocketService webSocketService = new WebSocketService(webSocketClient, false);
        webSocketService.connect();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        Web3j web3j = Web3j.build(webSocketService, 2000, executorService);

        System.out.println("开始监听地址 " + WATCH_ADDRESS + " 的ETH转账事件");

        web3j.blockFlowable(false).subscribe(block -> {
            EthBlock.Block ethBlock = block.getBlock();


            ethBlock.getTransactions().forEach(txResult -> {
                Object txObj = txResult.get();
                if (txObj instanceof String) {
                    String txHash = (String) txObj;
                    System.out.println("Tx Hash: " + txHash);

                    web3j.ethGetTransactionByHash(txHash).sendAsync().thenAccept(txResp -> {
                        txResp.getTransaction().ifPresent(tx -> {
                            String from = tx.getFrom() == null ? "" : tx.getFrom().toLowerCase();
                            String to = tx.getTo() == null ? "" : tx.getTo().toLowerCase();

                            if (WATCH_ADDRESS.equals(from) || WATCH_ADDRESS.equals(to)) {
                                System.out.println("From: " + tx.getFrom());
                                System.out.println("To: " + tx.getTo());
                                System.out.println("Value: " + tx.getValue());
                            }

                        });
                    });
                } else if (txObj instanceof EthBlock.TransactionObject) {
                    EthBlock.TransactionObject tx = (EthBlock.TransactionObject) txResult.get();
                    String from = tx.getFrom() == null ? "" : tx.getFrom().toLowerCase();
                    String to = tx.getTo() == null ? "" : tx.getTo().toLowerCase();

                    if (WATCH_ADDRESS.equals(from) || WATCH_ADDRESS.equals(to)) {
                        BigDecimal ethValue = Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER);

                        System.out.println("交易Hash: " + tx.getHash());
                        System.out.println("From: " + from);
                        System.out.println("To: " + to);
                        System.out.println("金额(ETH): " + ethValue);
                        System.out.println("区块号: " + ethBlock.getNumber());
                        System.out.println("--------");
                    }
                }
            });
        }, error -> {
            System.err.println("监听错误: " + error.getMessage());
        });

        Thread.currentThread().join();
    }
}
