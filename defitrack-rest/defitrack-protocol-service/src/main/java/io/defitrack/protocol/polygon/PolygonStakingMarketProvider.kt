package io.defitrack.protocol.polygon

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.POLYGON)
class PolygonStakingMarketProvider : FarmingMarketProvider() {

    val polygonStaking = "0x5e3ef299fddf15eaa0432e6e66473ace8c13d908"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = PolygonStakingContract(
            getBlockchainGateway(),
            polygonStaking
        )

        val staked = getToken(contract.token())

        return create(
            name = "Polygon Staking",
            identifier = polygonStaking,
            stakedToken = staked,
            rewardToken = staked,
            positionFetcher = PositionFetcher(
                contract::totalStakedForFn
            )
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.POLYGON
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}