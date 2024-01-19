package io.defitrack.protocol.dodo.pooling

import io.defitrack.common.utils.refreshable
import io.defitrack.domain.GetPriceCommand
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
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
                marketSize = refreshable {
                    getPriceResource().calculatePrice(
                        GetPriceCommand(
                            baseToken.address,
                            getNetwork(),
                            pool.baseReserve,
                            baseToken.type
                        )
                    ).toBigDecimal().plus(
                        getPriceResource().calculatePrice(
                            GetPriceCommand(
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