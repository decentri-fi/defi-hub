package io.defitrack.protocol.gains.staking

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.GAINS)
class PolygonGainsStakingMarketProvider : GainsStakingMarketProvider(
    "0x8c74b2256ffb6705f14ada8e86fbd654e0e2beca"
) {
    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}