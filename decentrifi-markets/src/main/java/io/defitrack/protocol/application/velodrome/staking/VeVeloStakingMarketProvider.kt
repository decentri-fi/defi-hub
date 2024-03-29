package io.defitrack.protocol.velodrome.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.VELODROME)
class VeVeloStakingMarketProvider : FarmingMarketProvider() {

    val veVelo = "0x9c7305eb78a432ced5c4d14cac27e8ed569a2e26"
    val velo = "0x3c8b650257cfb5f272f799f5e2b4e65093a11a05"
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val veloToken = getToken(velo)
        return listOf(
            create(
                name = "veVELO",
                identifier = "veVELO",
                stakedToken = veloToken,
                rewardToken = getToken(veVelo),
                type = "velodrome.v1.vevelo",
                marketSize = refreshable {
                    getMarketSize(veloToken, veVelo)
                },
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.VELODROME_V1
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}