package io.defitrack.protocol.makerdao.lending.market

import io.defitrack.common.network.Network
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.makerdao.MakerDAOEthereumGraphProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
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
                        marketToken = null
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