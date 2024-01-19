package io.defitrack.protocol.uniswap.v3.pooling

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.UNISWAP)
@ConditionalOnProperty(value = ["optimism.enabled", "uniswapv3.enabled"], havingValue = "true", matchIfMissing = true)
class UniswapV3OptimismPoolingPositionProvider(
    poolingMarketProvider: UniswapV3OptimismPoolingMarketProvider,
) : UniswapV3PoolingPositionProvider(
    poolingMarketProvider,
)