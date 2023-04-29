package io.defitrack.protocol.kyberswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.kyberswap.apr.KyberswapAPRService
import io.defitrack.protocol.kyberswap.graph.KyberswapPolygonGraphProvider
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class KyberswapPolygonPoolingMarketProvider(
    private val kyberswapPolygonGraphProvider: KyberswapPolygonGraphProvider,
    private val kyberswapAPRService: KyberswapAPRService,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        kyberswapPolygonGraphProvider.getPoolingMarkets().map {
            async {
                try {
                    val token = getToken(it.id)
                    val token0 = getToken(it.token0.id)
                    val token1 = getToken(it.token1.id)

                    create(
                        identifier = it.id,
                        address = it.id,
                        name = token.name,
                        symbol = token.symbol,
                        tokens = listOf(
                            token0.toFungibleToken(),
                            token1.toFungibleToken()
                        ),
                        apr = kyberswapAPRService.getAPR(it.pair.id, getNetwork()),
                        marketSize = it.reserveUSD,
                        tokenType = TokenType.KYBER,
                        positionFetcher = defaultPositionFetcher(token.address),
                        totalSupply = token.totalSupply
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}