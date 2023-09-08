package io.defitrack.protocol.uniswap.v3

import org.springframework.stereotype.Component

//@Component
class UniswapV3ArbitrumPoolingPositionProvider(
    uniswapV3ArbitrumPoolingMarketProvider: UniswapV3ArbitrumPoolingMarketProvider,
) : UniswapV3PoolingPositionProvider(
    uniswapV3ArbitrumPoolingMarketProvider,
)