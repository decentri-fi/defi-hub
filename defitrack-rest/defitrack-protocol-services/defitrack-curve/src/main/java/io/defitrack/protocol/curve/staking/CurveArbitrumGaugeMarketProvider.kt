package io.defitrack.protocol.curve.staking

import io.defitrack.common.network.Network
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = ["arbitrum.enabled"], havingValue = "true", matchIfMissing = true)
class CurveArbitrumGaugeMarketProvider : CurveGaugeFarmingMarketProvider(
    "0xabc000d88f23bb45525e447528dbf656a9d55bf5"
) {
    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}