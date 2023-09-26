package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["ethereum.enabled"], havingValue = "true", matchIfMissing = true)
class CurveEthereumPoolingMarketProvider(
) : CurvePoolingMarketProvider(
    "0x4F8846Ae9380B90d2E71D5e3D042dff3E7ebb40d"
) {
    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}