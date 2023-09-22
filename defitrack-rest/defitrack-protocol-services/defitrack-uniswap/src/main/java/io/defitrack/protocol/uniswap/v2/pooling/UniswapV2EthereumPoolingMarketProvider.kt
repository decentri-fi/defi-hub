package io.defitrack.protocol.uniswap.v2.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.protocol.Protocol
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
) : PoolingMarketProvider() {

    val factoryAddress = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f"

    val contract = lazyAsync {
        PairFactoryContract(
            getBlockchainGateway(),
            factoryAddress
        )
    }

    override suspend fun produceMarkets() = channelFlow {
        contract.await().allPairs().forEach {
            launch {
                throttled {
                    try {
                        val token = getToken(it)

                        val token0 = token.underlyingTokens[0]
                        val token1 = token.underlyingTokens[1]
                        val breakdown = fiftyFiftyBreakdown(
                            token0,
                            token1,
                            token.address
                        )

                        if (breakdown.sumOf { it.reserveUSD } > BigDecimal.valueOf(10000)) {
                            send(
                                create(
                                    identifier = "v2-${it}",
                                    name = token.name,
                                    address = it,
                                    symbol = token.symbol,
                                    tokens = listOf(
                                        token0.toFungibleToken(),
                                        token1.toFungibleToken()
                                    ),
                                    breakdown = breakdown,
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
                                        getToken(it).totalSupply.asEth(token.decimals)
                                    }
                                )
                            )
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        logger.error("something went wrong trying to import uniswap market ${it}")
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