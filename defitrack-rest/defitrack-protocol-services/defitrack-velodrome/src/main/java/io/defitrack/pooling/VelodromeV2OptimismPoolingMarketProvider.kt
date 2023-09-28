package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.VelodromeOptimismService
import io.defitrack.protocol.contract.PoolFactoryContract
import io.defitrack.token.TokenType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnProperty(value = ["velodromev2.enabled"], havingValue = "true", matchIfMissing = true)
class VelodromeV2OptimismPoolingMarketProvider(
    private val velodromeOptimismService: VelodromeOptimismService
) : PoolingMarketProvider() {


    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val pairFactoryContract = PoolFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = velodromeOptimismService.getV2PoolFactory()
        )

        pairFactoryContract.allPools().forEach {
            throttled {
                launch {
                    val poolingToken = getToken(it)
                    val tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken)

                    try {
                        val breakdown = fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address)
                        send(
                            create(
                                identifier = "v2-$it",
                                marketSize = refreshable(breakdown.sumOf { it.reserveUSD }) {
                                    fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address).sumOf { it.reserveUSD }
                                },
                                positionFetcher = defaultPositionFetcher(poolingToken.address),
                                address = it,
                                name = poolingToken.name,
                                breakdown = breakdown,
                                symbol = poolingToken.symbol,
                                tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                                tokenType = TokenType.VELODROME,
                                totalSupply = refreshable(poolingToken.totalSupply.asEth(poolingToken.decimals)) {
                                    getToken(it).totalSupply.asEth(poolingToken.decimals)
                                },
                                deprecated = false,
                                price = refreshable {
                                    BigDecimal.ZERO
                                }
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
        return Protocol.VELODROME_V2
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}