package io.defitrack.protocol.uniswap.v2.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.v2.pooling.prefetch.UniswapV2Prefetcher
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
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
                        val refreshableMarketSize = refreshable(
                            prefetch.marketSize ?: getMarketSize(
                                prefetch.tokens,
                                prefetch.address
                            )
                        ) {
                            getMarketSize(prefetch.tokens, prefetch.address)
                        }
                        val refreshableTotalSupply = refreshable(prefetch.totalSupply) {
                            getToken(prefetch.address).totalSupply.asEth(prefetch.decimals)
                        }
                        val refreshablePrice: Refreshable<BigDecimal> = calculatePrice(
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
                                symbol = prefetch.tokens.map { it.symbol }.joinToString("-"),
                                totalSupply = refreshableTotalSupply,
                                tokens = prefetch.tokens,
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