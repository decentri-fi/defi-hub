package io.defitrack.protocol.uniswap.v3

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("arbitrum.enabled", havingValue = "true", matchIfMissing = true)
class UniswapV3ArbitrumPoolingPositionProvider(
    uniswapV3ArbitrumPoolingMarketProvider: UniswapV3ArbitrumPoolingMarketProvider,
) : UniswapV3PoolingPositionProvider(
    uniswapV3ArbitrumPoolingMarketProvider,
)