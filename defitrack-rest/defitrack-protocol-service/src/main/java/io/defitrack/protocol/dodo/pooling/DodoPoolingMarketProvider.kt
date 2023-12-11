package io.defitrack.protocol.dodo.pooling

import io.defitrack.common.utils.Refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
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
                totalSupply = Refreshable.refreshable(BigDecimal.ZERO),
                marketSize = Refreshable.refreshable {
                    getPriceResource().calculatePrice(
                        PriceRequest(
                            baseToken.address,
                            getNetwork(),
                            pool.baseReserve,
                            baseToken.type
                        )
                    ).toBigDecimal().plus(
                        getPriceResource().calculatePrice(
                            PriceRequest(
                                quoteToken.address,
                                getNetwork(),
                                pool.quoteReserve,
                                baseToken.type
                            )
                        ).toBigDecimal()
                    )
                })
        }
    }
}