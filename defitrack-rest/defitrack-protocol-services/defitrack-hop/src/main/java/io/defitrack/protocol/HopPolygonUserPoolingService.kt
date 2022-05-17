package io.defitrack.protocol

import io.defitrack.pool.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class HopPolygonUserPoolingService(
    hopPolygonPoolingMarketService: HopPolygonPoolingMarketService,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(hopPolygonPoolingMarketService, erC20Resource)