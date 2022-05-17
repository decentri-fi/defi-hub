package io.defitrack.protocol.dmm

import io.defitrack.pool.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DMMEthereumUserPoolingService(
    dmmEthereumPoolingMarketService: DMMEthereumPoolingMarketService,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(dmmEthereumPoolingMarketService, erC20Resource)
