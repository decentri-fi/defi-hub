package io.defitrack.protocol.polygon

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.POLYGON)
class PolygonStakingMarketProvider : FarmingMarketProvider() {

    val polygonStaking = "0x5e3ef299fddf15eaa0432e6e66473ace8c13d908"

    val polygonStakingContract by lazy {
        PolygonStakingContract(
            getBlockchainGateway(),
            polygonStaking
        )
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val staked = getToken(polygonStakingContract.token())

        return listOf(
            create(
                name = "Polygon Staking",
                identifier = polygonStaking,
                stakedToken = staked.toFungibleToken(),
                rewardTokens = listOf(staked.toFungibleToken()),
                balanceFetcher = PositionFetcher(
                    polygonStaking,
                    {
                        polygonStakingContract.totalStakedForFn(it)
                    }
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.POLYGON
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}