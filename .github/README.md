# Defitrack Core

Defitrack core is the powerhouse of Defitrack.io. 

## Building the project

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

### Defitrack Protocols

Code that powers the underlying protocols.

### Defitrack Rest

Module that delivers the different rest endpoints that power https://defitrack.io.

- Price API
- ABI API
- ERC20 API
- API per Protocol