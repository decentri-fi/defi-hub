package io.defitrack.pooling

import io.defitrack.market.pooling.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SpiritFantomPoolingPositionProvider(
    spiritFantomPoolingMarketService: SpiritFantomPoolingMarketService,
    erC20Resource: ERC20Resource,
) : StandardLpPositionProvider(spiritFantomPoolingMarketService, erC20Resource)