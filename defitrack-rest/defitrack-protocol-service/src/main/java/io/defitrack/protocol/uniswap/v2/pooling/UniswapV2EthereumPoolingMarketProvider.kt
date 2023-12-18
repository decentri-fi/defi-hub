package io.defitrack.protocol.uniswap.v2.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.v2.pooling.prefetch.UniswapV2Prefetcher
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.UNISWAP)
@ConditionalOnProperty(value = ["ethereum.enabled", "uniswapv2.enabled"], havingValue = "true", matchIfMissing = true)
class UniswapV2EthereumPoolingMarketProvider(
    private val uniswapV2Prefetcher: UniswapV2Prefetcher
) : PoolingMarketProvider() {

    val factoryAddress = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f"

    val contract = lazyAsync {
        PairFactoryContract(
            getBlockchainGateway(),
            factoryAddress
        )
    }

    val prefetches = lazyAsync {
        uniswapV2Prefetcher.getPrefetches(getNetwork())
    }


    override suspend fun produceMarkets() = channelFlow {
        prefetches.await().forEach { prefetch ->
            launch {
                throttled {
                    try {

                        val breakdown = refreshable(
                            prefetch.breakdown?.map {
                                PoolingMarketTokenShare(
                                    it.token,
                                    it.reserve,
                                    it.reserveUSD
                                )
                            } ?: fiftyFiftyBreakdown(
                                prefetch.tokens[0],
                                prefetch.tokens[1],
                                prefetch.address
                            )
                        ) {
                            fiftyFiftyBreakdown(
                                prefetch.tokens[0],
                                prefetch.tokens[1],
                                prefetch.address
                            )
                        }


                        val refreshableMarketSize = breakdown.map {
                            it.sumOf(PoolingMarketTokenShare::reserveUSD)
                        }

                        val refreshableTotalSupply = refreshable(prefetch.totalSupply) {
                            getToken(prefetch.address).totalDecimalSupply()
                        }

                        val refreshablePrice = calculatePrice(
                            refreshableMarketSize, refreshableTotalSupply
                        )

                        send(
                            PoolingMarket(
                                id = prefetch.id,
                                network = prefetch.network.toNetwork(),
                                protocol = getProtocol(),
                                address = prefetch.address,
                                name = prefetch.name,
                                decimals = prefetch.decimals,
                                symbol = prefetch.tokens.joinToString("-") { it.symbol },
                                totalSupply = refreshableTotalSupply,
                                tokens = prefetch.tokens,
                                breakdown = breakdown,
                                marketSize = refreshableMarketSize,
                                deprecated = false,
                                internalMetadata = emptyMap(),
                                metadata = emptyMap(),
                                price = refreshablePrice,
                            )
                        )
                    } catch (ex: Exception) {
                        logger.info("Unable to get uniswap v2 market ${prefetch.name}")
                    }
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP_V2
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}