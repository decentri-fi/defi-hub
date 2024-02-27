package io.defitrack.protocol.application.curve.pooling

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CURVE)
@ConditionalOnProperty(value = ["base.enabled"], havingValue = "true", matchIfMissing = true)
class CurveBasePoolingMarketProvider : CurvePoolingMarketProvider(
    "0x3093f9b57a428f3eb6285a589cb35bea6e78c336"
) {
    override fun getNetwork(): Network {
        return Network.BASE
    }
}