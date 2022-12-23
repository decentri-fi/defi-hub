package io.defitrack.protocol.dodo.pooling

import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.DodoGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import io.defitrack.token.TokenType

abstract class DodoPoolingMarketProvider(
    erC20Resource: ERC20Resource,
    private val dodoGraphProvider: DodoGraphProvider,
    private val priceResource: PriceResource,
) : PoolingMarketProvider(erC20Resource) {
    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return dodoGraphProvider.getPools().map { pool ->

            val baseToken = getToken(pool.baseToken.id)
            val quoteToken = getToken(pool.quoteToken.id)

            PoolingMarket(
                id = "dodo-${getNetwork().slug}-${pool.id}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = pool.id,
                name = baseToken.symbol + "/" + quoteToken.symbol + " LP",
                symbol = baseToken.symbol + "/" + quoteToken.symbol,
                tokens = listOf(baseToken, quoteToken).map { it.toFungibleToken() },
                tokenType = TokenType.DODO,
                marketSize = priceResource.calculatePrice(
                    PriceRequest(
                        baseToken.address,
                        getNetwork(),
                        pool.baseReserve,
                        baseToken.type
                    )
                ).toBigDecimal().plus(
                    priceResource.calculatePrice(
                        PriceRequest(
                            quoteToken.address,
                            getNetwork(),
                            pool.quoteReserve,
                            baseToken.type
                        )
                    ).toBigDecimal()
                )
            )
        }
    }
}