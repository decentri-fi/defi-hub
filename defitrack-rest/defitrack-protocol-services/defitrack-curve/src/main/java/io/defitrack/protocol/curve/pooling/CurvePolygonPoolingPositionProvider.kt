package io.defitrack.protocol.curve.pooling

import io.defitrack.market.pooling.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class CurvePolygonPoolingPositionProvider(
    curvePolygonPoolingMarketService: CurveEthereumPoolingMarketService,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(curvePolygonPoolingMarketService, erC20Resource)