package io.defitrack.protocol.idex

import arrow.core.Either
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.IDEX)
class IdexPoolingMarketProvider(
    private val idexService: IdexService,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return idexService.getLPs().parMapNotNull(concurrency = 8) { lp ->
            Either.catch {
                createMarket(lp)
            }.mapLeft {
                logger.error("Unable to get pooling market {}: {}", lp.liquidityToken, it.message)
                null
            }.getOrNull()
        }
    }

    private suspend fun createMarket(it: IdexLP): PoolingMarket {
        val token = getToken(it.liquidityToken)
        val token0 = getToken(it.tokenA)
        val token1 = getToken(it.tokenB)

        return create(
            address = it.liquidityToken,
            symbol = token.symbol,
            identifier = it.liquidityToken,
            name = "IDEX ${token0.symbol}-${token1.symbol}",
            tokens = listOf(token0, token1),
            marketSize = refreshable(it.reserveUsd),
            positionFetcher = defaultPositionFetcher(token.address),
            totalSupply = refreshable {
                getToken(it.liquidityToken).totalDecimalSupply()
            }
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.IDEX
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}