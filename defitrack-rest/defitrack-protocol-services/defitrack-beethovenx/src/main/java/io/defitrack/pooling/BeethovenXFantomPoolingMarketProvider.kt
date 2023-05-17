package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.RefetchableValue
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

    override fun getProtocol(): Protocol {
        return Protocol.BEETHOVENX
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        beethovenXFantomGraphProvider.getPools().map {
            async {
                if (it.totalLiquidity > BigDecimal.valueOf(1000)) {
                    try {
                        create(
                            identifier = it.id,
                            address = it.address,
                            name = "${
                                it.tokens.joinToString("/", transform = PoolToken::symbol)
                            } Pool",
                            tokens = it.tokens.map { poolToken ->
                                getToken(poolToken.address).toFungibleToken()
                            },
                            symbol = it.symbol,
                            marketSize = RefetchableValue.refetchable(it.totalLiquidity),
                            tokenType = TokenType.BALANCER,
                            positionFetcher = defaultPositionFetcher(it.address),
                            totalSupply = RefetchableValue.refetchable(it.totalShares) //todo, use contracts to get total supply
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

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}