package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.QidaoArbitrumService
import io.defitrack.protocol.contract.QidaoVaultContract
import org.springframework.stereotype.Service
import java.math.BigDecimal

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
            vault.populateVaultOwners()

            val collateral = getToken(vault.collateral())

            create(
                name = vault.name(),
                identifier = vault.address,
                token = getToken(collateral.address).toFungibleToken(),
                marketSize = refreshable {
                    getMarketSize(
                        collateral.toFungibleToken(),
                        vault.address,
                    )
                },
                poolType = "mai_vault",
                metadata = mapOf(
                    "address" to vault.address,
                    "vaultContract" to vault
                ),
                marketToken = null,
                totalSupply = refreshable(BigDecimal.ZERO)
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