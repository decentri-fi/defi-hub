package io.defitrack.protocol.uniswap.v3.pooling

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.UNISWAP)
@ConditionalOnProperty(value = ["polygon.enabled", "uniswapv3.enabled"], havingValue = "true", matchIfMissing = true)
class UniswapV3PolygonPoolingPositionProvider(
    poolingMarketProvider: UniswapV3PolygonPoolingMarketProvider,
) : UniswapV3PoolingPositionProvider(
    poolingMarketProvider,
)