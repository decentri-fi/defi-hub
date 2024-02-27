package io.defitrack.protocol.application.aerodrome.pooling

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.contract.PoolFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AERODROME)
class AerodromePoolingMarketProvider : PoolingMarketProvider() {

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
                                totalSupply = refreshable {
                                    getToken(it).totalDecimalSupply()
                                },
                                type = "pool",
                                deprecated = false
                            )
                        )
                    } catch (ex: Exception) {
                        logger.error("Error while fetching pooling market $it: {}", ex.message)
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