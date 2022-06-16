package io.defitrack.protocol.apeswap

import io.defitrack.market.pooling.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class ApeswapPolygonPoolingPositionProvider(
    apeswapPolygonPoolingMarketProvider: ApeswapPolygonPoolingMarketProvider,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(
    apeswapPolygonPoolingMarketProvider, erC20Resource
)