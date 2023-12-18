package io.defitrack.protocol.alienbase.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.common.utils.toRefreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.ALIENBASE)
class AlienbasePoolingMarketProvider : PoolingMarketProvider() {

    private val poolFactoryAddress: String = "0x3e84d913803b02a4a7f027165e8ca42c14c0fde7"

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        PairFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = poolFactoryAddress
        ).allPairs().forEach {
            launch {
                throttled {
                    val poolingToken = getToken(it)
                    val tokens = poolingToken.underlyingTokens

                    try {
                        val breakdown = refreshable {
                            fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address)
                        }

                        send(
                            create(
                                identifier = it,
                                positionFetcher = defaultPositionFetcher(poolingToken.address),
                                address = it,
                                name = poolingToken.name,
                                breakdown = breakdown,
                                symbol = poolingToken.symbol,
                                tokens = poolingToken.underlyingTokens,
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