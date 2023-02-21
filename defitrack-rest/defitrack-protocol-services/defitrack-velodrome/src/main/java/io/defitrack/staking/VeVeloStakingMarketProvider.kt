package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
class VeVeloStakingMarketProvider : FarmingMarketProvider() {

    val veVelo = "0x9c7305eb78a432ced5c4d14cac27e8ed569a2e26"
    val velo = "0x3c8b650257cfb5f272f799f5e2b4e65093a11a05"
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val veloToken = getToken(velo)
        return listOf(
            create(
                name = "veVELO",
                identifier = "veVELO",
                stakedToken = veloToken.toFungibleToken(),
                rewardTokens = listOf(getToken(veVelo).toFungibleToken()),
                vaultType = "veVELO",
                marketSize = getMarketSize(veloToken.toFungibleToken(), veVelo),
                farmType = FarmType.VOTE_ESCROW
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.VELODROME
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}