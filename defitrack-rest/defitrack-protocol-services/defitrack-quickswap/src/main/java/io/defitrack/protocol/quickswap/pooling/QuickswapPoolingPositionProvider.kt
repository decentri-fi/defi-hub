package io.defitrack.protocol.quickswap.pooling

import io.defitrack.market.pooling.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class QuickswapPoolingPositionProvider(
    quickswapPoolingMarketProvider: QuickswapPoolingMarketProvider,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(quickswapPoolingMarketProvider, erC20Resource)