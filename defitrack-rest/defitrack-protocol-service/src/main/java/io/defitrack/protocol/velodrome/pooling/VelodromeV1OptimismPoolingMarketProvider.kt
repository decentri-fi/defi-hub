package io.defitrack.protocol.velodrome.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.VelodromeOptimismService
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.VELODROME)
@ConditionalOnProperty(value = ["velodromev1.enabled"], havingValue = "true", matchIfMissing = true)
class VelodromeV1OptimismPoolingMarketProvider(
    private val velodromeOptimismService: VelodromeOptimismService
) : PoolingMarketProvider() {


    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val pairFactoryContract = PairFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = velodromeOptimismService.getV1PoolFactory()
        )

        pairFactoryContract.allPairs().forEach {
            launch {
                throttled {
                    val poolingToken = getToken(it)
                    val tokens = poolingToken.underlyingTokens.map { it.toFungibleToken() }

                    try {
                        val breakdown = fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address)
                        send(
                            create(
                                identifier = "v1-$it",
                                marketSize = refreshable(breakdown.sumOf { it.reserveUSD } ) {
                                    fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address).sumOf { it.reserveUSD }
                                },
                                positionFetcher = defaultPositionFetcher(poolingToken.address),
                                address = it,
                                name = poolingToken.name,
                                breakdown = breakdown,
                                symbol = poolingToken.symbol,
                                tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                                totalSupply = refreshable(poolingToken.totalDecimalSupply()) {
                                    getToken(it).totalDecimalSupply()
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
        return Protocol.VELODROME_V1
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}