package io.defitrack.protocol.maple.market

import arrow.core.Either
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.maple.MapleFinanceEthereumGraphProvider
import io.defitrack.protocol.maple.Market
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.MAPLEFINANCE)
class MapleFinanceEthereumLendingMarketProvider(
    private val mapleFinanceEthereumGraphProvider: MapleFinanceEthereumGraphProvider,
) : LendingMarketProvider() {

    override suspend fun fetchMarkets(): List<LendingMarket> {
        return mapleFinanceEthereumGraphProvider.getMarkets().parMapNotNull(concurrency = 8) { pool ->
            Either.catch {
                createMarket(pool)
            }.mapLeft {
                logger.error("Failed to create market for ${pool.id}", it)
            }.getOrNull()
        }
    }

    private suspend fun createMarket(it: Market): LendingMarket {
        val token = getToken(it.id)

        return create(
            identifier = it.id,
            name = it.name,
            rate = it.rates.firstOrNull()?.rate?.toBigDecimal(),
            poolType = "maplefinance-lending",
            token = token,
        )
    }

    override fun getProtocol(): Protocol = Protocol.MAPLEFINANCE
    override fun getNetwork() = Network.ETHEREUM
}