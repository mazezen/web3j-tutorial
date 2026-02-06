package com.github.mazezen;


import com.github.mazezen.config.YamlConfig;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;


import java.io.IOException;

public class Web3ClientVersionClass {
    static void main() {
        String network = YamlConfig.getMainNetWorkNode("local");

        // to send synchronous requests
        Web3j web3 = Web3j.build(new HttpService(network));
        try {
            org.web3j.protocol.core.methods.response.Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
            String clientVersion = web3ClientVersion.getWeb3ClientVersion();

            System.out.println("client version: " + clientVersion);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
