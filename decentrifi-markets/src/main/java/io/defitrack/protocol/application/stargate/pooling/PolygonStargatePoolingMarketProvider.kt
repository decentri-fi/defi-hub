package io.defitrack.protocol.application.stargate.pooling

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.stargate.StargatePolygonService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
@ConditionalOnProperty(value = ["polygon.enabled"], havingValue = "true", matchIfMissing = true)
class PolygonStargatePoolingMarketProvider(
    stargateService: StargatePolygonService
) : AbstractStargatePoolingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}