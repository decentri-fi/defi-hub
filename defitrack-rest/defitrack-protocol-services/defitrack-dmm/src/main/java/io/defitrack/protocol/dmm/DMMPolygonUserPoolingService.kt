package io.defitrack.protocol.dmm

import io.defitrack.pool.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DMMPolygonUserPoolingService(
    dmmPolygonPoolingMarketService: DMMPolygonPoolingMarketService,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(dmmPolygonPoolingMarketService, erC20Resource)