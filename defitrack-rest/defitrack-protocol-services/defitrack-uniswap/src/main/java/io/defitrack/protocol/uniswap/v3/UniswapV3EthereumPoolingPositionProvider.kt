package io.defitrack.protocol.uniswap.v3

import org.springframework.stereotype.Component

@Component
class UniswapV3EthereumPoolingPositionProvider(
    uniswapV3EthereumPoolingMarketProvider: UniswapV3EthereumPoolingMarketProvider,
) : UniswapV3PoolingPositionProvider(
    uniswapV3EthereumPoolingMarketProvider,
)