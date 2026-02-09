// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "./ERC20.sol";

// 创建 Leaf ERC20 合约
contract Leaf is ERC20 {

    constructor(uint256 initialSupply) ERC20("Leaf Token", "LEAF") {
        // 初始供应量转账给合约部署者（合约创建者）
        _mint(msg.sender, initialSupply);
    }
}
