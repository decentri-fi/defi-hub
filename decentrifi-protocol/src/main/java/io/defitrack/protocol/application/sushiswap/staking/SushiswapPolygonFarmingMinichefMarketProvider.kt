package io.defitrack.protocol.application.sushiswap.staking

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.sushiswap.SushiPolygonService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SUSHISWAP)
class SushiswapPolygonFarmingMinichefMarketProvider : SushiMinichefV2FarmingMarketProvider(
    SushiPolygonService.getMiniChefs()
) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}