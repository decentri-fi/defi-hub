package io.defitrack.protocol.curve.staking

import io.defitrack.common.network.Network
import io.defitrack.pool.StandardLpPositionProvider
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class CurveEthereumUserPoolingService(
    private val curveEthereumPoolingMarketService: CurveEthereumPoolingMarketService,
    private val erC20Resource: ERC20Resource
) : StandardLpPositionProvider(curveEthereumPoolingMarketService, erC20Resource) {

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}