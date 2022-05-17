package io.defitrack.pooling

import io.defitrack.pool.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SpiritFantomUserPoolingService(
    spiritFantomPoolingMarketService: SpiritFantomPoolingMarketService,
    erC20Resource: ERC20Resource,
) : StandardLpPositionProvider(spiritFantomPoolingMarketService, erC20Resource)