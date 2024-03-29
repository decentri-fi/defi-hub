package io.defitrack.protocol.application.etherfi

import arrow.core.nel
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.etherfi.EEthContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.ETHER_FI)
class EEthStakingMarketProvider : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val eth = getToken("0x0")

        val eeth = with(getBlockchainGateway()) {
            EEthContract(
                "0x35fa164735182de50811e8e2e824cfb9b6118ac2"
            )
        }

        return create(
            name = "ether.fi ETH",
            identifier = "eeth",
            stakedToken = eth,
            rewardToken = eth,
            type = "etherfi.restaking",
            positionFetcher = defaultPositionFetcher(eeth.address),
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.ETHER_FI
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}