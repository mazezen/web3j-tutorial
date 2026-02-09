package com.github.mazezen;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.websocket.WebSocketService;

import java.util.Arrays;
import java.util.List;

public class Main {
    private static final String WS_URL = "";
    private static final String WATCH_ADDRESS = "".toLowerCase();

    static void main() throws Exception {
        WebSocketService webSocketService = new WebSocketService(WS_URL, true);
        webSocketService.connect();

        Web3j web3j = Web3j.build(webSocketService);

        List<String> erc20Contracts = List.of(
                "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", // USDC
                "0x6B175474E89094C44Da98b954EedeAC495271d0F"  // DAI
        );

        final Event transferEvent = new Event("Transfer", Arrays.asList(
                TypeReference.create(Address.class, true), // from indexed
                TypeReference.create(Address.class, true), // to indexed
                TypeReference.create(Uint256.class) // value
        ));

        String topic0 = EventEncoder.encode(transferEvent);
        String topic1 = "0x000000000000000000000000" + WATCH_ADDRESS.substring(2);
        String topic2 = "0x000000000000000000000000" + WATCH_ADDRESS.substring(2);

        for (String contractAddr : erc20Contracts) {
            EthFilter filter = new EthFilter(
                    DefaultBlockParameterName.LATEST,
                    DefaultBlockParameterName.LATEST, contractAddr
            );

            filter.addSingleTopic(topic0);
            filter.addOptionalTopics(topic1, topic2);

            web3j.ethLogFlowable(filter).subscribe(log -> {
               String from = "0x" + log.getTopics().get(1).substring(26);
               String to = "0x" + log.getTopics().get(2).substring(26);
                String txHash = log.getTransactionHash();
                String contract = log.getAddress();
                String valueHex = log.getData();

                java.math.BigInteger value = new java.math.BigInteger(valueHex.substring(2), 16);

                System.out.println("ERC20 Transfer detected:");
                System.out.println("Contract: " + contract);
                System.out.println("TxHash: " + txHash);
                System.out.println("From: " + from);
                System.out.println("To: " + to);
                System.out.println("Value (raw): " + value);
                System.out.println("--------------------------");
            }, error -> {
                System.err.println("Error in subscription: " + error);
            });

            System.out.println("Listening to ERC20 Transfer events for multiple contracts on address " + WATCH_ADDRESS);

            // 防止程序退出，保持监听
            Thread.sleep(Long.MAX_VALUE);
        }
    }
}
