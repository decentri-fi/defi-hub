package io.defitrack.protocol.dodo.pooling

import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarketElement
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.DodoGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType

abstract class DodoPoolingMarketProvider(
    private val erC20Resource: ERC20Resource,
    private val dodoGraphProvider: DodoGraphProvider,
    private val priceResource: PriceResource
) : PoolingMarketProvider() {
    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return dodoGraphProvider.getPools().map { pool ->

            val baseToken = erC20Resource.getTokenInformation(getNetwork(), pool.baseToken.id)
            val quoteToken = erC20Resource.getTokenInformation(getNetwork(), pool.quoteToken.id)

            PoolingMarketElement(
                id = "dodo-ethereum-${pool.id}",
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