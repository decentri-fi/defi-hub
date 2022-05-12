package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.pool.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class HopPolygonUserPoolingService(
    hopPolygonPoolingMarketService: HopPolygonPoolingMarketService,
    erC20Resource: ERC20Resource
) : StandardLpPositionProvider(hopPolygonPoolingMarketService, erC20Resource) {

    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}