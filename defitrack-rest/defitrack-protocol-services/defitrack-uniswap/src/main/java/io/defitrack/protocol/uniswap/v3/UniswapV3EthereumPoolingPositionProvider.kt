package io.defitrack.protocol.uniswap.v3

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("ethereum.enabled", havingValue = "true", matchIfMissing = true)
class UniswapV3EthereumPoolingPositionProvider(
    uniswapV3EthereumPoolingMarketProvider: UniswapV3EthereumPoolingMarketProvider,
) : UniswapV3PoolingPositionProvider(
    uniswapV3EthereumPoolingMarketProvider,
)