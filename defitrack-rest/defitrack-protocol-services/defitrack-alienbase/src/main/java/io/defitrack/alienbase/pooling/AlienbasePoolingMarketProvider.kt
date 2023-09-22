package io.defitrack.alienbase.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
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
import org.springframework.stereotype.Component

@Component
class AlienbasePoolingMarketProvider(
) : PoolingMarketProvider() {

    private val poolFactoryAddress: String = "0x3e84d913803b02a4a7f027165e8ca42c14c0fde7"

    private val poolFactoryContract = lazyAsync {
        PairFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = poolFactoryAddress
        )
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        poolFactoryContract.await().allPairs().forEach {
            launch {
                throttled {
                    val poolingToken = getToken(it)
                    val tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken)

                    try {
                        val breakdown = fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address)
                        send(
                            create(
                                identifier = it,
                                marketSize = refreshable(breakdown.sumOf { it.reserveUSD }) {
                                    getMarketSize(
                                        poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                                        it
                                    )
                                },
                                positionFetcher = defaultPositionFetcher(poolingToken.address),
                                address = it,
                                name = poolingToken.name,
                                breakdown = breakdown,
                                symbol = poolingToken.symbol,
                                tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                                tokenType = TokenType.VELODROME,
                                totalSupply = refreshable(poolingToken.totalSupply.asEth(poolingToken.decimals)) {
                                    with(getToken(it)) {
                                        totalSupply.asEth(decimals)
                                    }
                                },
                                deprecated = true
                            )
                        )
                    } catch (ex: Exception) {
                        logger.error("Error while fetching pooling market $it", ex)
                    }
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.ALIENBASE
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}