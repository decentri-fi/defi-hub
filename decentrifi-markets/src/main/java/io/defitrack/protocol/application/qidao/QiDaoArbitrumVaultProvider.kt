package io.defitrack.protocol.application.qidao

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.qidao.QidaoArbitrumService
import io.defitrack.protocol.qidao.contract.QidaoVaultContract
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.QIDAO)
class QiDaoArbitrumVaultProvider(
    private val qidaoArbitrumService: QidaoArbitrumService,
) : LendingMarketProvider() {
    override suspend fun fetchMarkets(): List<LendingMarket> {
        return qidaoArbitrumService.provideVaults().parMap {

            val vault = with(getBlockchainGateway()) { QidaoVaultContract(it) }
            val collateral = getToken(vault.collateral.await())

            create(
                name = vault.name(),
                identifier = vault.address,
                token = getToken(collateral.address),
                marketSize = refreshable {
                    getMarketSize(
                        collateral,
                        vault.address,
                    )
                },
                poolType = "mai_vault",
                metadata = mapOf(
                    "address" to vault.address,
                    "vaultContract" to vault
                ),
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