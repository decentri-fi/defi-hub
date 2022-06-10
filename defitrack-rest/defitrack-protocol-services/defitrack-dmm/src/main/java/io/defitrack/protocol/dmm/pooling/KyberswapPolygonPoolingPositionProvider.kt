package io.defitrack.protocol.dmm.pooling

import io.defitrack.pool.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class KyberswapPolygonPoolingPositionProvider(
    kyberswapPolygonPoolingMarketProvider: KyberswapPolygonPoolingMarketProvider,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(kyberswapPolygonPoolingMarketProvider, erC20Resource)