# Decentrifi DeFi Hub

A comprehensive API for indexing and querying DeFi-related data across multiple blockchain networks.

## Overview

Decentrifi DeFi Hub is a modular, multi-chain API that enables developers and applications to access standardized DeFi data including:

- Token balances and prices
- Market information (lending, farming, pooling)
- Protocol statistics
- Claimable rewards
- On-chain events
- NFT data
- ENS resolution

The project supports multiple EVM-compatible blockchains and provides a unified interface for accessing DeFi data across these chains.

## Supported Networks

- Ethereum
- Polygon
- Polygon zkEVM
- Arbitrum
- Optimism
- Base

## Project Structure

The project is organized into several modules:

- **defitrack-common**: Common utilities and shared functionality
- **decentrifi-client**: Client libraries for consuming the API
    - Balance client
    - ERC20 client
    - Price client
    - Protocol client
    - Event domain
- **decentrifi-markets**: Market definitions and providers
- **defitrack-blockchains**: Blockchain-specific implementations
    - Ethereum
    - Polygon
    - Polygon zkEVM
    - Arbitrum
    - Optimism
    - Base
- **defitrack-protocol-contracts**: Contract integrations for various DeFi protocols
- **defitrack-rest**: REST API endpoints
    - Balance API
    - ERC20 API
    - Events API
    - Claimables API
    - Protocol Service API
    - Price API
    - ENS API
    - NFT API
    - Statistics API
    - EVM API

## Core Features

### Price Tracking
Get token prices across multiple networks with automatic fallbacks and cross-network alternatives for stable coins.

```
GET /{token-address}?network={network}
```

### Balance Tracking
Query token balances, native balances, and full portfolio data for any address.

```
GET /{user}/token-balances
GET /{user}/native-balance
GET /{user}/{token}?network={network}
```

### Market Data
Access standardized market data for:
- Lending markets
- Farming (staking) markets
- Pooling markets (liquidity)
- Borrowing markets

```
GET /markets?protocol={protocol}&network={network}
```

### Position Tracking
Track user positions across different DeFi protocols.

```
GET /positions/{protocol}/{user}?network={network}
```

### Claimable Rewards
Discover claimable rewards and airdrops for any address.

```
GET /claimables/{user}
```

### Events
Decode and analyze on-chain events.

```
GET /events/decode
```

### Statistics
Get aggregate statistics about DeFi protocols.

```
GET /statistics/{protocol}/lending
GET /statistics/{protocol}/farming
GET /statistics/{protocol}/pooling
```

## Technology Stack

- **Language**: Kotlin
- **Framework**: Spring Boot 3.2.4
- **Build Tool**: Maven
- **Blockchain Integration**: Web3j 4.10.3
- **Async Programming**: Kotlin Coroutines
- **HTTP Client**: Ktor Client
- **Caching**: Cache4k
- **Documentation**: Swagger/OpenAPI
- **Containerization**: Docker
- **CI/CD**: Jenkins

## Getting Started

### Prerequisites
- JDK 21
- Maven 3.x
- Docker (for containerized deployment)

### Building the Project
```bash
mvn clean package
```

### Running the Application
```bash
java -jar defitrack-rest/defitrack-api-gw/target/defitrack-api-gw-0.0.1-SNAPSHOT.jar
```

### Docker Deployment
```bash
docker build -t defitrack -f ci/Dockerfile .
docker run -p 8080:8080 defitrack
```

## API Documentation

API documentation is available via Swagger UI when the application is running:

```
http://localhost:8080/swagger-ui.html
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please read our [Code of Conduct](CODE_OF_CONDUCT.md) before contributing to the project.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.