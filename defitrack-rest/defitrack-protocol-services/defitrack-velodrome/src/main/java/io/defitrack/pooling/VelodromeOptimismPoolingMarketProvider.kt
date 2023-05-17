package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.common.utils.RefetchableValue.Companion.refetchable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.VelodromeOptimismService
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class VelodromeOptimismPoolingMarketProvider(
    private val velodromeOptimismService: VelodromeOptimismService
) : PoolingMarketProvider() {


    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val pairFactoryContract = PairFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = velodromeOptimismService.getPoolFactory()
        )

        pairFactoryContract.allPairs().forEach {
            launch {
                throttled {
                    val poolingToken = getToken(it)
                    val tokens = poolingToken.underlyingTokens

                    try {
                        send(
                            create(
                                identifier = it,
                                marketSize = refetchable {
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
                                totalSupply = refetchable(poolingToken.totalSupply.asEth(poolingToken.decimals)) {
                                    val poolingToken = getToken(it)
                                    poolingToken.totalSupply.asEth(poolingToken.decimals)
                                },
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
        return Protocol.VELODROME
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}