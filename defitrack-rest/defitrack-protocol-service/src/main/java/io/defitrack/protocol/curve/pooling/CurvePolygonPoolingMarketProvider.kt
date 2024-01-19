package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CURVE)
@ConditionalOnProperty(value = ["polygon.enabled"], havingValue = "true", matchIfMissing = true)
class CurvePolygonPoolingMarketProvider : CurvePoolingMarketProvider(
    "0x722272d36ef0da72ff51c5a65db7b870e2e8d4ee"
) {
    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}