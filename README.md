# scala-basic-blockchain
Proof-of-concept implementation of block chain in Scala


This project is a scala POC implementation of a [python basic block chain implementation](https://hackernoon.com/learn-blockchains-by-building-one-117428612f46)

# Usage

Build the block chain jar file using make.ps1 (on Windows) or make.sh (on Unix), this will create the jar file
[basic-blockchain.jar](basic-blockchain.jar)

Now run the following command:

```bash
java -jar basic-blockchain.jar
```

This will start the block chain node at http://localhost:3088

Now to run a second basic block chain node:

```bash
java -jar basic-blockchain.jar http://localhost:3088
```

This will start the second block chain node at http://localhost:3089 and uses the node at http://localhost:3088 as the
seed node to broadcast its ip address.

The following api is available for the block chain:

* http://localhost:3088/mine: mine by running proof-of-work in the current node
* http://localhost:3088/nodes/resolve: achieve consensus in the block chain network by resolving conflicts
* http://localhost:3088/transactions/new: add a new transaction to the current node
* http://localhost:3088/chain: return the chain stored in the current node
* http://localhost:3088/nodes: return the list of nodes participating in the block chain
