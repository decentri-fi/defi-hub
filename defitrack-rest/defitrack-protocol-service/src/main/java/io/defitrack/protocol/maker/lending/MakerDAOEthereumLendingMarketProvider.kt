package io.defitrack.protocol.maker.lending

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.makerdao.MakerDAOEthereumGraphProvider
import io.defitrack.protocol.makerdao.domain.Market
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.MAKERDAO)
class MakerDAOEthereumLendingMarketProvider(private val makerDAOEthereumGraphProvider: MakerDAOEthereumGraphProvider) :
    LendingMarketProvider() {

    override suspend fun fetchMarkets(): List<LendingMarket> {
        return makerDAOEthereumGraphProvider.getMarkets().parMapNotNull(concurrency = 8) { pool ->
            catch {
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
            poolType = "makerdao-lending",
            token = token,
            marketToken = null,
            totalSupply = refreshable(BigDecimal.ZERO)
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.MAKERDAO
    }

    override fun getNetwork() = Network.ETHEREUM
}