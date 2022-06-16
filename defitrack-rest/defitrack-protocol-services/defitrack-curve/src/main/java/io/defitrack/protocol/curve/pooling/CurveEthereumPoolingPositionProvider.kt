package io.defitrack.protocol.curve.pooling

import io.defitrack.market.pooling.StandardLpPositionProvider
import io.defitrack.protocol.curve.pooling.CurveEthereumPoolingMarketService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class CurveEthereumPoolingPositionProvider(
    curveEthereumPoolingMarketService: CurveEthereumPoolingMarketService,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(curveEthereumPoolingMarketService, erC20Resource)