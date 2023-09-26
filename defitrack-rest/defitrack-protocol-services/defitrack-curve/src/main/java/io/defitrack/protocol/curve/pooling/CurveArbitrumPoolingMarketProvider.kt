package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["arbitrum.enabled"], havingValue = "true", matchIfMissing = true)
class CurveArbitrumPoolingMarketProvider(
) : CurvePoolingMarketProvider(
"0xb17b674d9c5cb2e441f8e196a2f048a81355d031"
) {
    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}
