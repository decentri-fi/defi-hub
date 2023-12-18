package io.defitrack.protocol.apeswap

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.refreshable
import io.defitrack.common.utils.toRefreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.APESWAP)
class ApeswapPolygonPoolingMarketProvider(
    private val apeswapPolygonService: ApeswapPolygonService
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.APESWAP
    }

    val pools = lazyAsync {
            val pairFactoryContract = PairFactoryContract(
                blockchainGateway = getBlockchainGateway(),
                contractAddress = apeswapPolygonService.provideFactory()
            )
            pairFactoryContract.allPairs()
    }


    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val semaphore = Semaphore(16)

        return coroutineScope {
            pools.await().map { pool ->
                semaphore.withPermit {
                    async {
                        try {
                            val poolingToken = getToken(pool)
                            val underlyingTokens = poolingToken.underlyingTokens

                            val breakdown =
                                fiftyFiftyBreakdown(underlyingTokens[0], underlyingTokens[1], poolingToken.address)
                            create(
                                identifier = pool,
                                address = pool,
                                name = poolingToken.name,
                                symbol = poolingToken.symbol,
                                marketSize = refreshable(breakdown.sumOf { it.reserveUSD }) {
                                   breakdown.sumOf { it.reserveUSD }
                                },
                                breakdown = breakdown.toRefreshable(),
                                tokens = underlyingTokens,
                                positionFetcher = defaultPositionFetcher(poolingToken.address),
                                totalSupply = refreshable {
                                    getToken(pool).totalDecimalSupply()
                                }
                            )
                        } catch (ex: Exception) {
                            logger.error("Error while fetching pooling market $pool", ex)
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}