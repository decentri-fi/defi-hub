package io.defitrack.protocol.sushiswap.staking

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.SushiPolygonService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SUSHISWAP)
class SushiswapPolygonFarmingMinichefMarketProvider(
) : SushiMinichefV2FarmingMarketProvider(
    SushiPolygonService.getMiniChefs()
) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}