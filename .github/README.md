# Defitrack Core

Defitrack core is the powerhouse of Defitrack.io. 
It's the general API that is used by https://defitrack.io and contains all the code
to interact with various protocols, networks and general web3 things.

## Building the project

The project is entirely written in Kotlin.

Prerequisites:
- java 8+

```shell
./mvn clean package
```

## Submodules

### Defitrack Blockchains

Code that powers the underlying chains. 

- Ethereum
- Polygon
- BSC
- Arbitrum
- Fantom
- Avalanche
- Arbitrum
- Optimism

### Defitrack Protocols

Code that powers the underlying protocols.

- Aave
- Adamant 
- Balancer
- Beefy
- Compound
- Convex
- Curve
- Dfyn
- Dinoswap
- Kyberswap (Formerly DMM)
- Idex
- Jarvis
- MakerDAO
- MapleFinance
- mStable
- Polycat
- Quickswap
- Spiritswap
- SpookySwap
- Sushiswap
- Uniswap
- Yearn

### Defitrack Rest

Module that delivers the different rest endpoints that power https://defitrack.io.

- General API Gateway
- Price API
- Balance API
- ABI API
- ERC20/Token API
- API per Protocol
- API per network