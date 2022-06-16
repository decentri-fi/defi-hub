package io.defitrack.protocol.uniswap.pooling

import io.defitrack.market.pooling.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class UniswapPoolingPositionProvider(
    ethereumPoolingMarketService: UniswapEthereumPoolingMarketService,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(ethereumPoolingMarketService, erC20Resource)