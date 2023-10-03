package io.defitrack.protocol.uniswap.v3

import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.UNISWAP)
@ConditionalOnProperty(value = ["arbitrum.enabled", "uniswapv3.enabled"], havingValue = "true", matchIfMissing = true)
class UniswapV3ArbitrumPoolingPositionProvider(
    uniswapV3ArbitrumPoolingMarketProvider: UniswapV3ArbitrumPoolingMarketProvider,
) : UniswapV3PoolingPositionProvider(
    uniswapV3ArbitrumPoolingMarketProvider,
)