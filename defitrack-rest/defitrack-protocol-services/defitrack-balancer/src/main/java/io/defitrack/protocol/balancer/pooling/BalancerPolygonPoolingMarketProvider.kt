package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.graph.BalancerPolygonPoolGraphProvider
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class BalancerPolygonPoolingMarketProvider(
    private val balancerPolygonPoolGraphProvider: BalancerPolygonPoolGraphProvider,
) : PoolingMarketProvider() {
    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {

        val semaphore = Semaphore(8)

        val pools = balancerPolygonPoolGraphProvider.getPools()
        pools.map {
            async {
                try {
                    semaphore.withPermit {
                        val token = getToken(it.address)

                        if (it.totalLiquidity > BigDecimal.valueOf(100000)) {
                            PoolingMarket(
                                id = "balancer-polygon-${it.id}",
                                network = getNetwork(),
                                protocol = getProtocol(),
                                address = it.address,
                                name = "${
                                    it.tokens.joinToString("/") {
                                        it.symbol
                                    }
                                } Pool",
                                tokens = token.underlyingTokens.map {
                                    it.toFungibleToken()
                                },
                                symbol = it.symbol,
                                apr = BigDecimal.ZERO,
                                marketSize = it.totalLiquidity,
                                tokenType = TokenType.BALANCER,
                                positionFetcher = defaultPositionFetcher(it.address),
                                totalSupply = it.totalShares.toBigInteger()
                            )
                        } else {
                            null
                        }
                    }
                } catch (ex: Exception) {
                    logger.error("Error while fetching balancer pool", ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol = Protocol.BALANCER

    override fun getNetwork(): Network = Network.POLYGON
}