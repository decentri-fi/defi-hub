package io.defitrack.protocol.application.dodo.pooling

import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.dodo.DodoGraphProvider
import java.math.BigDecimal

abstract class DodoPoolingMarketProvider(
    private val dodoGraphProvider: DodoGraphProvider,
) : PoolingMarketProvider() {
    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return dodoGraphProvider.getPools().map { pool ->

            val baseToken = getToken(pool.baseToken.id)
            val quoteToken = getToken(pool.quoteToken.id)

            create(
                identifier = pool.id,
                address = pool.id,
                name = baseToken.symbol + "/" + quoteToken.symbol + " LP",
                symbol = baseToken.symbol + "/" + quoteToken.symbol,
                tokens = listOf(baseToken, quoteToken),
                totalSupply = refreshable(BigDecimal.ZERO),
            )
        }
    }
}