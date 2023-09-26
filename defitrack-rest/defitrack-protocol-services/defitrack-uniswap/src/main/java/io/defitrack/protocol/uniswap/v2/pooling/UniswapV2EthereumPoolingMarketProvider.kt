package io.defitrack.protocol.uniswap.v2.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.v2.pooling.prefetch.UniswapV2Prefetcher
import io.defitrack.token.TokenType
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
        contract.await().allPairs().forEach {address ->
            launch {
                throttled {
                    try {
                        val identifier = "v2-${address}"

                        val prefetch = prefetches.await().find {
                            it.id == createId(identifier)
                        }

                        val token = getToken(address)

                        val token0 = prefetch?.tokens?.get(0) ?: token.underlyingTokens[0].toFungibleToken()
                        val token1 = prefetch?.tokens?.get(1) ?:  token.underlyingTokens[1].toFungibleToken()
                        val breakdown = prefetch?.breakdown?.map {
                            PoolingMarketTokenShare(
                                it.token,
                                it.reserve,
                                it.reserveUSD
                            )
                        } ?: fiftyFiftyBreakdown(token0, token1, address)


                        val marketSize = breakdown.sumOf {
                            it.reserveUSD
                        }

                        if (breakdown.sumOf { it.reserveUSD } > BigDecimal.valueOf(50000)) {
                            send(
                                create(
                                    identifier = identifier,
                                    name = token.name,
                                    address = address,
                                    symbol = token.symbol,
                                    tokens = listOf(token0, token1),
                                    breakdown = breakdown,
                                    marketSize = refreshable(marketSize) {
                                        fiftyFiftyBreakdown(token0, token1, address).sumOf {
                                            it.reserveUSD
                                        }
                                    },
                                    tokenType = TokenType.UNISWAP,
                                    positionFetcher = defaultPositionFetcher(token.address),
                                    totalSupply = refreshable(token.totalSupply.asEth(token.decimals)) {
                                        getToken(address).totalSupply.asEth(token.decimals)
                                    }
                                )
                            )
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        logger.error("something went wrong trying to import uniswap market ${address}")
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