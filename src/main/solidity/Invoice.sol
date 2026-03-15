// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

interface IERC20 {
    function transferFrom(address from, address to, uint256 amount) external returns (bool);
}

contract Invoice {
    address public owner;

    struct InvoiceData {
        string invoiceId;
        address sender;
        address recipient;
        address token;
        uint256 amount;
        bool executed;
        bool exists;
    }

    mapping(bytes32 => InvoiceData) private invoices;

    event InvoiceCreated(
        bytes32 indexed invoiceKey,
        string invoiceId,
        address indexed sender,
        address indexed recipient,
        address token,
        uint256 amount
    );

    event InvoiceExecuted(
        bytes32 indexed invoiceKey,
        string invoiceId,
        address indexed sender,
        address indexed recipient,
        address token,
        uint256 amount
    );

    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can call this");
        _;
    }

    constructor() {
        owner = msg.sender;
    }

    function createInvoice(
        string memory invoiceId,
        address sender,
        address recipient,
        address token,
        uint256 amount
    ) external onlyOwner {
        require(bytes(invoiceId).length > 0, "invoiceId is required");
        require(sender != address(0), "Invalid sender");
        require(recipient != address(0), "Invalid recipient");
        require(token != address(0), "Invalid token");
        require(amount > 0, "Amount must be greater than zero");

        bytes32 invoiceKey = keccak256(abi.encodePacked(invoiceId));
        require(!invoices[invoiceKey].exists, "Invoice already exists");

        invoices[invoiceKey] = InvoiceData({
            invoiceId: invoiceId,
            sender: sender,
            recipient: recipient,
            token: token,
            amount: amount,
            executed: false,
            exists: true
        });

        emit InvoiceCreated(invoiceKey, invoiceId, sender, recipient, token, amount);
    }

    function executeInvoice(string memory invoiceId) external onlyOwner {
        bytes32 invoiceKey = keccak256(abi.encodePacked(invoiceId));
        InvoiceData storage inv = invoices[invoiceKey];

        require(inv.exists, "Invoice does not exist");
        require(!inv.executed, "Invoice already executed");

        bool success = IERC20(inv.token).transferFrom(
            inv.sender,
            inv.recipient,
            inv.amount
        );
        require(success, "ERC20 transfer failed");

        inv.executed = true;

        emit InvoiceExecuted(
            invoiceKey,
            inv.invoiceId,
            inv.sender,
            inv.recipient,
            inv.token,
            inv.amount
        );
    }

    function getInvoice(string memory invoiceId) external view returns (InvoiceData)
    {
        bytes32 invoiceKey = keccak256(abi.encodePacked(invoiceId));
        InvoiceData memory inv = invoices[invoiceKey];
        return inv;
    }

    function transferOwnership(address newOwner) external onlyOwner {
        require(newOwner != address(0), "Invalid new owner");
        owner = newOwner;
    }
}
