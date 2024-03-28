package io.defitrack.protocol.application.camelot.farming

import arrow.core.nonEmptyListOf
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CAMELOT)
class CamelotXGrailFarmingMarketProvider : FarmingMarketProvider() {

    val xGrail = "0x3caae25ee616f2c8e13c74da0813402eae3f496b"
    val grail = "0x3d9907f9a368ad0a51be60f7da3b97cf940982d8"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val staked = getToken(grail)
        return nonEmptyListOf(
            create(
                name = "xGRAIL",
                identifier = xGrail,
                stakedToken = staked,
                rewardToken = staked,
                type = "camelot.farming",
                positionFetcher = defaultPositionFetcher(xGrail)
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.CAMELOT
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}