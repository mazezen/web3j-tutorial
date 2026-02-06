package com.github.mazezen;

import com.github.mazezen.config.YamlConfig;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;

public class AccountClass {
    static void main() {
        String network = YamlConfig.getMainNetWorkNode("local");
        Web3j web3j = Web3j.build(new HttpService(network));

        String address = "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1";
        getEthBalance(web3j, address);
    }

    /**
     * 获取账户 ETH 余额
     *
     * @param web3j
     * @param address
     */
    public static void getEthBalance(Web3j web3j, String address) {
        try {
            EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            System.out.println("address: " + address + " balance id: " + balance.getBalance());
            BigDecimal ethBalance = Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER);
            System.out.println("address: " + address + " balance id: " + ethBalance.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
