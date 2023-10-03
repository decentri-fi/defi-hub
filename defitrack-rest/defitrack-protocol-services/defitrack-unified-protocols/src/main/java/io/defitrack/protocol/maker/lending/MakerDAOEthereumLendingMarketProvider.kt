package io.defitrack.protocol.maker.lending

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.makerdao.MakerDAOEthereumGraphProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.MAKERDAO)
class MakerDAOEthereumLendingMarketProvider(
    private val makerDAOEthereumGraphProvider: MakerDAOEthereumGraphProvider,
) : LendingMarketProvider() {

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        makerDAOEthereumGraphProvider.getMarkets().map {
            async {
                try {
                    val token = getToken(it.id)

                    create(
                        identifier = it.id,
                        name = it.name,
                        rate = it.rates.firstOrNull()?.rate?.toBigDecimal(),
                        poolType = "makerdao-lending",
                        token = token.toFungibleToken(),
                        marketToken = null,
                        totalSupply = Refreshable.refreshable(BigDecimal.ZERO)
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.MAKERDAO
    }

    override fun getNetwork() = Network.ETHEREUM
}