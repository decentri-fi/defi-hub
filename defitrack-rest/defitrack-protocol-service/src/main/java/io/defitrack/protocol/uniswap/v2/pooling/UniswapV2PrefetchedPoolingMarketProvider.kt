package io.defitrack.protocol.uniswap.v2.pooling

import arrow.fx.coroutines.parMap
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.v2.pooling.prefetch.UniswapV2Prefetcher
import kotlinx.coroutines.flow.channelFlow
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnNetwork(Network.ETHEREUM)
@ConditionalOnCompany(Company.UNISWAP)
@ConditionalOnProperty(value = ["uniswapv2.enabled"], havingValue = "true", matchIfMissing = true)
class UniswapV2PrefetchedPoolingMarketProvider(
    private val prefetchers: UniswapV2Prefetcher
) : PoolingMarketProvider() {

    override suspend fun produceMarkets() = channelFlow {
        logger.info("Generating Uniswap V2 markets, with prefetching enabled")
        val prefetches = prefetchers.getPrefetches(getNetwork())
        prefetches.parMap(concurrency = 8) { prefetch ->
            try {
                val breakdown = refreshable(
                    fiftyFiftyBreakdown(
                        prefetch.tokens[0].toFungibleToken(getNetwork()),
                        prefetch.tokens[1].toFungibleToken(getNetwork()),
                        prefetch.address
                    )
                ) {
                    fiftyFiftyBreakdown(
                        getToken(prefetch.tokens[0].address),
                        getToken(prefetch.tokens[1].address),
                        prefetch.address
                    )
                }


                val refreshableTotalSupply = refreshable(prefetch.totalSupply) {
                    getToken(prefetch.address).totalDecimalSupply()
                }

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
                        tokens = prefetch.tokens.map {
                            it.toFungibleToken(getNetwork())
                        },
                        breakdown = breakdown,
                        deprecated = false,
                        internalMetadata = emptyMap(),
                        metadata = emptyMap()
                    )
                )
            } catch (ex: Exception) {
                logger.info("Unable to get uniswap v2 market ${prefetch.name}")
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