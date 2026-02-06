package com.github.mazezen;

import com.github.mazezen.config.YamlConfig;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.exception.CipherException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class TransferClass {

    static void main() {
        String network = YamlConfig.getMainNetWorkNode("local");

        Web3j web3j = Web3j.build(new HttpService(network));


        String fromPrivateKey = "";
        String to = "";
        BigInteger amount = Convert.toWei("1", Convert.Unit.ETHER).toBigInteger(); // 0.1 eth

        transferEth(web3j, fromPrivateKey, to, amount);
        transferEIP1559(web3j, fromPrivateKey, to, amount);

    }
    /**
     * 交易 eth
     *
     * @param web3j
     * @param fromPrivateKey
     * @param to
     * @param amount
     */
    public static void transferEth(Web3j web3j, String fromPrivateKey, String to, BigInteger amount) {

       try {
           Credentials credentials = Credentials.create(fromPrivateKey);
           TransactionReceipt tx = Transfer.sendFunds(web3j, credentials, to, new BigDecimal(amount), Convert.Unit.WEI).send();
           System.out.println("tx id: " + tx.getTransactionHash());
           System.out.println("transaction info: " + tx.toString());

       } catch (InterruptedException e ){
           System.out.println("Interrupted exception: " + e.getMessage());
       } catch (IOException e) {
           System.out.println("Io exception: " + e.getMessage());
       } catch (TransactionException e) {
           System.out.println("create transaction exception: " + e.getMessage());
       } catch (CipherException e) {
           System.out.println("cipher exception: " + e.getMessage());
       } catch (Exception e) {
           System.out.println("exception: " + e.getMessage());
       }
    }

    /**
     * 交易 eth
     *
     * @param web3j
     * @param fromPrivateKey
     * @param to
     * @param amount
     */
    public static void transferEIP1559(Web3j web3j, String fromPrivateKey, String to, BigInteger amount) {
        try {
            Credentials credentials = Credentials.create(fromPrivateKey);
            TransactionReceipt tx = Transfer.sendFundsEIP1559(
                    web3j,
                    credentials,
                    to,
                    new BigDecimal(amount),
                    Convert.Unit.WEI,
                    BigInteger.valueOf(8_000_000), // gasLimit
                    DefaultGasProvider.GAS_LIMIT, // maxPriorityFeePerGas (max fee per gas transaction willing to give to miners)
                    BigInteger.valueOf(3_100_000_000L) // maxFeePerGas (max fee transaction willing to pay)
            ).send();
            System.out.println("tx id: " + tx.getTransactionHash());
            System.out.println("tx type: " + tx.getType());
            System.out.println("tx status: " + tx.getStatus());
            System.out.println("transaction info: " + tx.toString());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
