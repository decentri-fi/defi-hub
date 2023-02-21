package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.QidaoArbitrumService
import io.defitrack.protocol.contract.QidaoVaultContract
import org.springframework.stereotype.Service

@Service
class QiDaoArbitrumVaultProvider(
    private val qidaoArbitrumService: QidaoArbitrumService,
) : LendingMarketProvider() {
    override suspend fun fetchMarkets(): List<LendingMarket> {

        return qidaoArbitrumService.provideVaults().map {

            val vault = QidaoVaultContract(
                getBlockchainGateway(),
                it
            )

            val collateral = erC20Resource.getTokenInformation(getNetwork(), vault.collateral())

            create(
                name = vault.name(),
                identifier = vault.address,
                token = erC20Resource.getTokenInformation(getNetwork(), collateral.address).toFungibleToken(),
                marketSize = marketSizeService.getMarketSize(
                    collateral.toFungibleToken(),
                    vault.address,
                    getNetwork()
                ),
                poolType = "mai_vault",
                positionFetcher = defaultPositionFetcher(vault.address)
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.QIDAO
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}