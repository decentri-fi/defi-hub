package io.defitrack.protocol

import io.defitrack.market.pooling.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class HopPolygonPoolingPositionProvider(
    hopPolygonPoolingMarketService: HopPolygonPoolingMarketService,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(hopPolygonPoolingMarketService, erC20Resource)