package io.defitrack.protocol.camelot

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

//@Component
class CamelotArbitrumPoolingMarketProvider(
) : PoolingMarketProvider() {

    val pools = lazyAsync {
        val pairFactoryContract = PairFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = "0x6eccab422d763ac031210895c81787e87b43a652"
        )
        pairFactoryContract.allPairs()
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        return channelFlow {
            pools.await().forEach { pool ->
                launch {
                    try {
                        val poolingToken = getToken(pool)
                        val underlyingTokens = poolingToken.underlyingTokens.map { it.toFungibleToken() }


                        val breakdown =
                            fiftyFiftyBreakdown(underlyingTokens[0], underlyingTokens[1], poolingToken.address)
                        send(
                            create(
                                identifier = pool,
                                address = pool,
                                name = poolingToken.name,
                                symbol = poolingToken.symbol,
                                marketSize = refreshable(breakdown.sumOf { it.reserveUSD }) {
                                    getMarketSize(
                                        poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                                        pool
                                    )
                                },
                                breakdown = breakdown,
                                tokens = underlyingTokens,
                                positionFetcher = defaultPositionFetcher(poolingToken.address),
                                totalSupply = refreshable(poolingToken.totalDecimalSupply()) {
                                    getToken(pool).totalDecimalSupply()
                                },
                                price = refreshable { BigDecimal.ZERO }
                            )
                        )
                    } catch (ex: Exception) {
                        logger.error("Error while fetching pooling market $pool", ex)
                    }
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CAMELOT
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}