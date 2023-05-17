package io.defitrack.protocol.camelot

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.common.utils.RefetchableValue
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        return channelFlow {
            pools.forEach { pool ->
                launch {
                    try {
                        val poolingToken = getToken(pool)
                        val underlyingTokens = poolingToken.underlyingTokens


                        send(
                            create(
                                identifier = pool,
                                address = pool,
                                name = poolingToken.name,
                                symbol = poolingToken.symbol,
                                marketSize = RefetchableValue.refetchable {
                                    getMarketSize(
                                        poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                                        pool
                                    )
                                },
                                breakdown = defaultBreakdown(underlyingTokens, poolingToken.address),
                                tokens = underlyingTokens.map { it.toFungibleToken() },
                                tokenType = TokenType.CAMELOT,
                                positionFetcher = defaultPositionFetcher(poolingToken.address),
                                totalSupply = RefetchableValue.refetchable(poolingToken.totalDecimalSupply()) {
                                    getToken(pool).totalDecimalSupply()
                                }
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