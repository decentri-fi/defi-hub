package io.defitrack.protocol.camelot

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

//@Component
class CamelotArbitrumPoolingMarketProvider(
) : PoolingMarketProvider() {


    val pools by lazy {
        runBlocking {
            val pairFactoryContract = PairFactoryContract(
                blockchainGateway = getBlockchainGateway(),
                contractAddress = "0x6eccab422d763ac031210895c81787e87b43a652"
            )
            pairFactoryContract.allPairs()
        }
    }


    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return coroutineScope {
            pools.map { pool ->
                throttled {
                    async {
                        try {
                            val poolingToken = getToken(pool)
                            val underlyingTokens = poolingToken.underlyingTokens
                            val marketSize = getMarketSize(
                                poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                                pool
                            )

                            create(
                                identifier = pool,
                                address = pool,
                                name = poolingToken.name,
                                symbol = poolingToken.symbol,
                                marketSize = marketSize,
                                breakdown = defaultBreakdown(underlyingTokens, poolingToken.address),
                                tokens = underlyingTokens.map { it.toFungibleToken() },
                                tokenType = TokenType.CAMELOT,
                                positionFetcher = defaultPositionFetcher(poolingToken.address),
                                totalSupply = poolingToken.totalSupply
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

    override fun getProtocol(): Protocol {
        return Protocol.CAMELOT
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}