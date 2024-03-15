package io.defitrack.protocol.application.blast

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.blast.BlastDepositContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BLAST)
class BlastDepositFarmingMarketProvider : FarmingMarketProvider() {

    val blastDepositAddress = "0x5f6ae08b8aeb7078cf2f96afb089d7c9f51da47d"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return listOf(ethMarket(), usdMarket())
    }

    private suspend fun usdMarket(): FarmingMarket {
        val dai = getToken("0x6B175474E89094C44Da98b954EedeAC495271d0F")

        val contract = BlastDepositContract(
            blockchainGateway = getBlockchainGateway(),
            address = blastDepositAddress
        )

        return create(
            name = "Blast Deposit",
            identifier = blastDepositAddress + "-dai",
            stakedToken = dai,
            rewardTokens = emptyList(),
            type = "blast.deposits",
            positionFetcher = PositionFetcher(contract::usdShares)
        )
    }

    private suspend fun ethMarket(): FarmingMarket {
        val eth = getToken("0x0")

        val contract = BlastDepositContract(
            blockchainGateway = getBlockchainGateway(),
            address = blastDepositAddress
        )

        return create(
            name = "Blast Deposit",
            identifier = blastDepositAddress + "-eth",
            stakedToken = eth,
            rewardTokens = emptyList(),
            type = "blast-deposits",
            positionFetcher = PositionFetcher(contract::ethShares)
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.BLAST
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}