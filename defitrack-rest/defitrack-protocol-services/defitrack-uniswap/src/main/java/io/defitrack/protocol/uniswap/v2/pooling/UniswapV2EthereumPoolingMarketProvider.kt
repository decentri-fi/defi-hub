package io.defitrack.protocol.uniswap.v2.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.v2.apr.UniswapAPRService
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v2.AbstractUniswapV2Service
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("ethereum.enabled", havingValue = "true", matchIfMissing = true)
class UniswapV2EthereumPoolingMarketProvider(
    private val uniswapServices: List<AbstractUniswapV2Service>,
    private val uniswapAPRService: UniswapAPRService,
) : PoolingMarketProvider() {

    override suspend fun produceMarkets() = channelFlow {
        uniswapServices.filter {
            it.getNetwork() == getNetwork()
        }.forEach { service ->
            service.getPairs().forEach {
                launch {
                    throttled {
                        try {
                            val token = getToken(it.id)
                            val token0 = getToken(it.token0.id)
                            val token1 = getToken(it.token1.id)

                            var breakdown = fiftyFiftyBreakdown(
                                token0,
                                token1,
                                token.address
                            )

                            send(
                                create(
                                    identifier = "v2-${it.id}",
                                    name = token.name,
                                    address = it.id,
                                    symbol = token.symbol,
                                    tokens = listOf(
                                        token0.toFungibleToken(),
                                        token1.toFungibleToken()
                                    ),
                                    breakdown = breakdown,
                                    apr = uniswapAPRService.getAPR(it.id, getNetwork()),
                                    marketSize = refreshable(
                                        breakdown.sumOf {
                                            it.reserveUSD
                                        }
                                    ) {
                                        fiftyFiftyBreakdown(
                                            token0,
                                            token1,
                                            token.address
                                        ).sumOf {
                                            it.reserveUSD
                                        }
                                    },
                                    tokenType = TokenType.UNISWAP,
                                    positionFetcher = defaultPositionFetcher(token.address),
                                    totalSupply = refreshable(token.totalSupply.asEth(token.decimals)) {
                                        getToken(it.id).totalSupply.asEth(token.decimals)
                                    }
                                )
                            )
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            logger.error("something went wrong trying to import uniswap market ${it.id}")
                        }

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