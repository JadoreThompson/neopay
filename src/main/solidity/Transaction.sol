// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

interface IERC20 {
    function transferFrom(address from, address to, uint256 amount) external returns (bool);
    function balanceOf(address account) external view returns (uint256);
}

contract Transaction {
    address public owner;

    struct TransactionData {
        string transactionId;
        address sender;
        address recipient;
        address token;
        uint256 amount;
        bool executed;
        bool exists;
        bool failed;
    }

    mapping(bytes32 => TransactionData) private transactions;

    event TransactionCreated(
        bytes32 indexed transactionKey,
        string transactionId,
        address indexed sender,
        address indexed recipient,
        address token,
        uint256 amount,
        uint256 timestamp
    );

    event TransactionExecuted(
        bytes32 indexed transactionKey,
        string transactionId,
        address indexed sender,
        address indexed recipient,
        address token,
        uint256 amount,
        uint256 timestamp
    );

    event TransactionFailed(
        bytes32 indexed transactionKey,
        string transactionId,
        address indexed sender,
        address indexed recipient,
        address token,
        uint256 amount,
        string reason,
        uint256 timestamp
    );

    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can call this");
        _;
    }

    constructor() {
        owner = msg.sender;
    }

    function createTransaction(
        string memory transactionId,
        address sender,
        address recipient,
        address token,
        uint256 amount
    ) external onlyOwner {
        require(bytes(transactionId).length > 0, "transactionId is required");
        require(sender != address(0), "Invalid sender");
        require(recipient != address(0), "Invalid recipient");
        require(token != address(0), "Invalid token");
        require(amount > 0, "Amount must be greater than zero");

        bytes32 transactionKey = keccak256(abi.encodePacked(transactionId));
        require(!transactions[transactionKey].exists, "Transaction already exists");

        transactions[transactionKey] = TransactionData({
            transactionId: transactionId,
            sender: sender,
            recipient: recipient,
            token: token,
            amount: amount,
            executed: false,
            exists: true,
            failed: false
        });

        emit TransactionCreated(
            transactionKey,
            transactionId,
            sender,
            recipient,
            token,
            amount,
            block.timestamp
        );
    }

    function executeTransaction(string memory transactionId) external onlyOwner {
        bytes32 transactionKey = keccak256(abi.encodePacked(transactionId));
        TransactionData storage txn = transactions[transactionKey];

        require(txn.exists, "Transaction does not exist");
        require(!txn.executed, "Transaction already executed");

        uint256 senderBalance = IERC20(txn.token).balanceOf(txn.sender);

        if (senderBalance < txn.amount) {
            txn.failed = true;

            emit TransactionFailed(
                transactionKey,
                txn.transactionId,
                txn.sender,
                txn.recipient,
                txn.token,
                txn.amount,
                "Insufficient token balance",
                block.timestamp
            );
            return;
        }

        bool success = IERC20(txn.token).transferFrom(
            txn.sender,
            txn.recipient,
            txn.amount
        );

        if (!success) {
            txn.failed = true;

            emit TransactionFailed(
                transactionKey,
                txn.transactionId,
                txn.sender,
                txn.recipient,
                txn.token,
                txn.amount,
                "ERC20 transfer failed",
                block.timestamp
            );
            return;
        }

        txn.executed = true;
        txn.failed = false;

        emit TransactionExecuted(
            transactionKey,
            txn.transactionId,
            txn.sender,
            txn.recipient,
            txn.token,
            txn.amount,
            block.timestamp
        );
    }

    function getTransaction(string memory transactionId)
    external
    view
    returns (
        string memory transactionId_,
        address sender,
        address recipient,
        address token,
        uint256 amount,
        bool executed,
        bool exists,
        bool failed
    )
    {
        bytes32 transactionKey = keccak256(abi.encodePacked(transactionId));
        TransactionData memory txn = transactions[transactionKey];

        return (
            txn.transactionId,
            txn.sender,
            txn.recipient,
            txn.token,
            txn.amount,
            txn.executed,
            txn.exists,
            txn.failed
        );
    }

    function transferOwnership(address newOwner) external onlyOwner {
        require(newOwner != address(0), "Invalid new owner");
        owner = newOwner;
    }
}