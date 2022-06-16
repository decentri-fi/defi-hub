package io.defitrack.protocol.quickswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.StandardLpPositionProvider
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class QuickswapPoolingPositionProvider(
    quickswapPoolingMarketService: QuickswapPoolingMarketService,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(quickswapPoolingMarketService, erC20Resource)