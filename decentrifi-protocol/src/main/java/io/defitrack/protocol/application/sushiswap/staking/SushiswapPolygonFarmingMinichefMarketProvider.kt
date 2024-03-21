package io.defitrack.protocol.application.sushiswap.staking

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SUSHISWAP)
class SushiswapPolygonFarmingMinichefMarketProvider : SushiMinichefV2FarmingMarketProvider(
    listOf("0x0769fd68dfb93167989c6f7254cd0d766fb2841f")
) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}