package io.defitrack.protocol.dfyn.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.StandardLpPositionProvider
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DfynPoolingPositionProvider(
    dfynPoolingMarketService: DfynPoolingMarketService,
    erC20Resource: ERC20Resource,
) : StandardLpPositionProvider(dfynPoolingMarketService, erC20Resource)