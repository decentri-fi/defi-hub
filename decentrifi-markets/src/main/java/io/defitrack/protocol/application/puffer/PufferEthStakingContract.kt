package io.defitrack.protocol.application.puffer

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@ConditionalOnNetwork(Network.ETHEREUM)
@ConditionalOnCompany(Company.PIKA)
@Component
class PufferEthStakingContract : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return listOf(
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.PUFFER
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}