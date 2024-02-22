package io.defitrack.protocol.etherfi

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.price.port.`in`.PricePort
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.lido.LidoService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.ETHER_FI)
class EEthStakingMarketProvider : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val eth = getToken("0x0")

        val eeth = EEthContract(
            getBlockchainGateway(),
            "0x35fa164735182de50811e8e2e824cfb9b6118ac2"
        )

        return listOf(
            create(
                name = "ether.fi ETH",
                identifier = "eeth",
                stakedToken = eth,
                rewardToken = eth,
                positionFetcher = defaultPositionFetcher(eeth.address),
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.LIDO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}