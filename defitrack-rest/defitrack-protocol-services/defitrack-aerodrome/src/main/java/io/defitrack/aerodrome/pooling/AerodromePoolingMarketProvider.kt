package io.defitrack.aerodrome.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.PoolFactoryContract
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class AerodromePoolingMarketProvider(
) : PoolingMarketProvider() {

    private val poolFactoryAddress: String = "0x420DD381b31aEf6683db6B902084cB0FFECe40Da"

    private val poolFactoryContract = lazyAsync {
        PoolFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = poolFactoryAddress
        )
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
            poolFactoryContract.await().allPools().forEach {
            launch {
                throttled {
                    val poolingToken = getToken(it)
                    val tokens = poolingToken.underlyingTokens

                    try {
                        send(
                            create(
                                identifier = it,
                                marketSize = Refreshable.refreshable {
                                    getMarketSize(
                                        poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                                        it
                                    )
                                },
                                positionFetcher = defaultPositionFetcher(poolingToken.address),
                                address = it,
                                name = poolingToken.name,
                                breakdown = defaultBreakdown(tokens, poolingToken.address),
                                symbol = poolingToken.symbol,
                                tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                                tokenType = TokenType.VELODROME,
                                totalSupply = Refreshable.refreshable(poolingToken.totalSupply.asEth(poolingToken.decimals)) {
                                    val poolingToken = getToken(it)
                                    poolingToken.totalSupply.asEth(poolingToken.decimals)
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