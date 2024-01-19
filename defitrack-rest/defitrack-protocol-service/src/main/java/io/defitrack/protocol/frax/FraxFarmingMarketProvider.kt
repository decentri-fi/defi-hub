package io.defitrack.protocol.frax

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.FRAX)
class FraxFarmingMarketProvider : FarmingMarketProvider() {

    val farms = listOf(
        "0x3ef26504dbc8dd7b7aa3e97bc9f3813a9fc0b4b0",

    )

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return emptyList()
    }

    override fun getProtocol(): Protocol {
        return Protocol.FRAX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}