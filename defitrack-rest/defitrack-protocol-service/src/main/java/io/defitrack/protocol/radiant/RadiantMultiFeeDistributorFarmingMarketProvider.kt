package io.defitrack.protocol.radiant

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
class RadiantMultiFeeDistributorFarmingMarketProvider : FarmingMarketProvider() {

    val address = "0x8f2dfc78f10af047356f87039634e829175813f5"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        /*
                val contract = RadiantMultiFeeDistributor(
                    getBlockchainGateway(),
                    address
                )

                return listOf(
                    create(
                        name = "Radiant Staking",
                        identifier = address,

                        )
                ) */
        return emptyList()
    }

    override fun getProtocol(): Protocol {
        return Protocol.RADIANT
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}