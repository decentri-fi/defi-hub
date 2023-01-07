package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.PoolToken
import io.defitrack.protocol.graph.BeethovenXFantomGraphProvider
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BeethovenXFantomPoolingMarketProvider(
    private val beethovenXFantomGraphProvider: BeethovenXFantomGraphProvider
) : PoolingMarketProvider() {
    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        beethovenXFantomGraphProvider.getPools().map {
            async {
                if (it.totalLiquidity > BigDecimal.valueOf(100000)) {
                    try {
                        PoolingMarket(
                            id = "beets-fantom-${it.id}",
                            network = getNetwork(),
                            protocol = getProtocol(),
                            address = it.address,
                            name = "${
                                it.tokens.joinToString("/", transform = PoolToken::symbol)
                            } Pool",
                            tokens = it.tokens.map { poolToken ->
                                getToken(poolToken.address).toFungibleToken()
                            },
                            symbol = it.symbol,
                            apr = BigDecimal.ZERO,
                            marketSize = it.totalLiquidity,
                            tokenType = TokenType.BALANCER,
                            positionFetcher = defaultPositionFetcher(it.address)
                        )
                    } catch (ex: Exception) {
                        logger.error("problem trying to import beethoven pool", ex)
                        null
                    }
                } else {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.BEETHOVENX
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}