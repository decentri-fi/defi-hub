package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = ["optimism.enabled"], havingValue = "true", matchIfMissing = true)
class CurveOptimismPoolingMarketProvider : CurvePoolingMarketProvider(
    "0x2db0e83599a91b508ac268a6197b8b14f5e72840"
) {
    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}