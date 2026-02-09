# Web3j-Tutorial
> <a href="https://docs.web3j.io/4.14.0">Web3j 版本 4.14.0</a>
>
> 实例代码,连接是本地虚拟机中的节点模拟器.
>

## 一 web3j-examples
> 示例
* Web3ClientVersionClass 获取客户端版本
* nowBlockNumber 最新区块高度
* transferEth 转账 Eth
* transferEIP1559 基于 EIP-1559 提案转账 Eth
* getEthBalance 获取账户 Eth 余额 

## 二 web3j-deployERC20Contract
> 部署 ERC20 合约 Leaf Token
1. 安装 <a href="https://docs.soliditylang.org/en/latest/installing-solidity.html#macos-packages">solidity编译器</a> (Mac brew)
```shell
brew update
brew upgrade
brew tap ethereum/ethereum
brew install solidity
```
查看 solc 版本
```shell
solc --version
```
2. 编译 LeafERC20 合约.获取合约的字节码和 ABI。
```shell
$ solc --optimize --bin --abi LeafERC20.sol --evm-version paris -o output
Compiler run successful. Artifact(s) can be found in directory "output".
```
这个命令会生成两个文件,位于 output目录下：
* Leaf.bin：包含了合约的字节码。
* Leaf.abi：包含了合约的 ABI（用于与合约交互的接口）。

3. 使用 Web3j Command Line Tools 根据 ABI 文件生成 Java 类
```shell
web3j generate solidity -b Leaf.bin -a Leaf.abi -o ./src/main/java -p com.mazezen.github
```
4. 运行 DeployLeafToken 完成合约部署

## 三 web3j-DeFi
> ERC20授权代理转账流程（Approve & TransferFrom Pattern）
* Approve：代币持有者授权另一个地址（代理/平台）代表自己使用一定数量的代币
* TransferFrom：代理地址代表代币持有者发起实际的转账操作，把代币转给第三方

1. 给 sender 地址充值 LEAF
* 部署合约账户（Deployer） 转账 100 LEAF 给 sender 地址（0xffcf8fdee...）
* 作用：让 sender 拥有代币余额，才能执行后续操作

2. sender 地址调用 approve 授权
* sender 地址调用 approve(platform, 100 LEAF)，授权 platform 账户可以代表它最多使用 100 个 LEAF。
* 作用：允许 platform 作为代理，帮 sender 操作最多 100 个代币。
3. platform 调用 transferFrom 转账
* platform 使用 transferFrom(sender, receiver, 100 LEAF)，把 sender 的 100 LEAF 转给 receiver。
* 作用：通过代理身份，完成实际转账给第三方

3. 每个地址的角色
* deployer(代币部署者) - 部署合约及初始化代币账户  -   代币初始化供应持有者
* sender  - 代币持有人 - 有余额,授权给platform 使用代币
* platform - 代理账户 - 被授权,可以代表sender 转账代币给receiver 
* receiver - 收款账户 - 终端收到代币的账户

## 四 web3j-monitorScan
> 监听. 监听地址发生交易触发

## 五 web3j-monitorScanErc20
> 多合约监听. 监听地址发生ERC20合约交易触发

# 前置条件
## 安装 Web3j command cli
```shell
curl -L get.web3j.io | sh && source ~/.web3j/source.sh
```

## 安装本地节点模拟器.
> 通过 VMware Fusion 安装的 Ubuntu24.04.3
1. 安装 <a href="https://geth.ethereum.org/">geth</a> (Ubuntu via PPAs)

The following command enables the launchpad repository:
```shell
sudo add-apt-repository -y ppa:ethereum/ethereum
```

Then, to install the stable version of go-ethereum:
```shell
sudo apt-get update
sudo apt-get install ethereum
```
启动geth并连接到指定的测试网如: Sepolia | Goerli:

```shell
geth --sepolia --http --http.port 8545 --http.api web3,eth,personal,net
geth --goerli --http --http.port 8545 --http.api web3,eth,personal,net
```
启动私有链时,通常需要为你的私有链生成一个创世块文件. 创世块是以太坊区块链的第一个区块.可以自定义它以适应你的私有链配置.
使用 geth 初始化创世块：
```shell
geth init genesis.json
```
genesis.json
```json
{
  "config": {
    "chainId": 1,
    "homesteadBlock": 0,
    "eip155Block": 0,
    "eip158Block": 0
  },
  "alloc": {},
  "difficulty": "0x20000",
  "gasLimit": "0x21000",
  "timestamp": "0x0"
}
```
启动一个私有链:
```shell
geth --networkid 1 --datadir ./mydatadir --http --http.port 8545 --http.api web3,eth,personal,net,miner
```
启动一个私有链并启用挖矿:
```shell
geth --networkid 1 --datadir ./mydatadir --http --http.port 8545 --http.api web3,eth,personal,net,miner --mine --miner.threads 1
```


* --networkid：用于指定网络 ID，区分不同网络（如主网、测试网或私有链）
* --datadir：指定存储区块链数据的目录。
* --http：启用 HTTP-RPC 服务。
* --http.port：指定 RPC 服务的端口号。
* --mine：启用挖矿功能。
* --syncmode：指定同步模式（如 full, fast, light）。


2. 安装 <a href="https://github.com/ConsenSys-archive/ganache">Ganache模拟器</a>
    
需要安装 <a href="https://nodejs.org/zh-cn/download">Node 和 npm:</a>
```shell
# 下载并安装 nvm：
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
# 代替重启 shell
\. "$HOME/.nvm/nvm.sh"
# 下载并安装 Node.js：
nvm install 24
# 验证 Node.js 版本：
node -v # Should print "v24.13.0".
# 验证 npm 版本：
npm -v # Should print "11.6.2".
```
执行完脚本复制以下内容到 .profile 中:
```shell
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"  # This loads nvm
[ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"  # This loads nvm bash_completion
```
To install Ganache globally, run:
```shell
npm install ganache --global
```
运行Ganache:
```shell
ganache-cli --host 0.0.0.0 --port 8545 --deterministic --db ./data
```
--host 0.0.0.0
* --host 参数用于指定 Ganache 服务监听的主机地址。
* 0.0.0.0 表示监听所有的网络接口地址，即它会接受来自任何 IP 地址的连接。这对于希望在局域网内或者多个设备之间进行交互的情况非常有用。例如，如果你想让其他设备也能访问你的 Ganache 服务，就需要使用 0.0.0.0 而不是默认的 127.0.0.1（localhost）。

--port 8545
* --port 参数指定 Ganache 要监听的端口号。默认的端口是 8545，所以这个参数指定 Ganache 服务通过端口 8545 提供访问。
* 如果你不指定 --port，Ganache 会使用默认端口 8545。可以修改这个参数来避免与其他服务的端口冲突，或者用其他端口来进行自定义配置。

--deterministic
* --deterministic 参数表示启用确定性账户生成。默认情况下，Ganache 在每次启动时会生成不同的账户和私钥。
* 使用 --deterministic 参数后，Ganache 会始终生成相同的账户和私钥，确保每次启动模拟器时的账户地址和余额都相同。这样对于开发和测试来说更加稳定和可复现，尤其在团队协作或者多次测试同一合约时非常有用。
* --db ./data 指定了一个文件夹来持久化数据，所有的账户、交易、区块等都会存储在该文件夹中。每次启动时，ganache-cli 会加载该目录中的数据，并恢复之前的状态。




