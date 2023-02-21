package io.defitrack.protocol.apeswap

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ApeswapPolygonPoolingMarketProvider(
    private val apeswapPolygonService: ApeswapPolygonService
) : PoolingMarketProvider() {


    val pools by lazy {
        runBlocking {
            val pairFactoryContract = PairFactoryContract(
                blockchainGateway = getBlockchainGateway(),
                contractAddress = apeswapPolygonService.provideFactory()
            )
            pairFactoryContract.allPairs()
        }
    }


    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val semaphore = Semaphore(16)

        return coroutineScope {
            pools.map { pool ->
                async {
                    try {
                        semaphore.withPermit {
                            val poolingToken = getToken(pool)
                            val underlyingTokens = poolingToken.underlyingTokens
                            val marketSize = getMarketSize(
                                poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                                pool
                            )

                            if (marketSize < BigDecimal.valueOf(10000))
                                return@async null

                            create(
                                identifier = pool,
                                address = pool,
                                name = poolingToken.name,
                                symbol = poolingToken.symbol,
                                marketSize = marketSize,
                                breakdown = defaultBreakdown(underlyingTokens, poolingToken.address),
                                tokens = underlyingTokens.map { it.toFungibleToken() },
                                tokenType = TokenType.APE,
                                positionFetcher = defaultPositionFetcher(poolingToken.address)
                            )
                        }
                    } catch (ex: Exception) {
                        logger.error("Error while fetching pooling market $pool", ex)
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.APESWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}