package io.defitrack.protocol.maplefinance.lending.market

import io.defitrack.common.network.Network
import io.defitrack.market.RefetchableValue.Companion.refetchable
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.maplefinance.MapleFinanceEthereumGraphProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class MapleFinanceEthereumLendingMarketProvider(
    private val mapleFinanceEthereumGraphProvider: MapleFinanceEthereumGraphProvider,
) : LendingMarketProvider() {

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        mapleFinanceEthereumGraphProvider.getMarkets().map {
            async {
                try {
                    val token = getToken(it.id)

                    create(
                        identifier = it.id,
                        name = it.name,
                        rate = it.rates.firstOrNull()?.rate?.toBigDecimal(),
                        poolType = "maplefinance-lending",
                        token = token.toFungibleToken(),
                        marketToken = null,
                        totalSupply = refetchable(BigDecimal.ZERO),
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.MAPLEFINANCE
    }

    override fun getNetwork() = Network.ETHEREUM
}