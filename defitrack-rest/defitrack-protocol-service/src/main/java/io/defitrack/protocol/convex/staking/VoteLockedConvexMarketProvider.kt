package io.defitrack.protocol.convex.staking

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CONVEX)
class VoteLockedConvexMarketProvider : FarmingMarketProvider() {

    val vICVXAddress = "0x72a19342e8f1838460ebfccef09f6585e32db86e"
    val cvxAddress = "0x4e3fbd56cd56c3e72c1403e103b45db9da5b9d2b"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        //todo: rewards are actually different
        val cvx = getToken(cvxAddress)

        return listOf(
            create(
                name = "Vote-Locked Convex",
                identifier = vICVXAddress,
                stakedToken = cvx,
                rewardToken = cvx,
                positionFetcher = defaultPositionFetcher(vICVXAddress)
            )
        )
    }


    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}