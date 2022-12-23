# Decentrifi

Decentrifi is an open source API which allows you to easily integrate DeFi in any application. It provides a gateway to web3 without having to know any of the underlying cryptographic technology.

## Decentrifi Core

Defitrack core is the powerhouse of decentrifi. 
It's the general API that is used by https://decentri.fi and contains all the code
to interact with various protocols, networks and general web3 things.

## Building the project

The project is entirely written in Kotlin.

Prerequisites:
- java 8+

```shell
./mvn clean package
```

## Submodules

### Decentrifi Blockchains

Code that powers the underlying chains. 

- Ethereum
- Polygon
- BSC
- Arbitrum
- Fantom
- Avalanche
- Arbitrum
- Optimism

### Decentrifi Protocols

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

### Decentrifi Rest

Module that delivers the different rest endpoints that power https://decentri.fi.

- General API Gateway
- Price API
- Balance API
- ABI API
- ERC20/Token API
- API per Protocol
- API per network