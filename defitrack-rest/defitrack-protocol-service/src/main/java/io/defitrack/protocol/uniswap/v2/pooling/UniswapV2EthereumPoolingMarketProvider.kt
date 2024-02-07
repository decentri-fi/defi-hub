package io.defitrack.protocol.uniswap.v2.pooling

import arrow.core.Either
import arrow.fx.coroutines.parMap
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.port.out.Prices
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.v2.pooling.prefetch.UniswapV2Prefetcher
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnNetwork(Network.ETHEREUM)
@ConditionalOnCompany(Company.UNISWAP)
@ConditionalOnProperty(value = ["uniswapv2.fresh.enabled"], havingValue = "true", matchIfMissing = false)
class UniswapV2EthereumPoolingMarketProvider(
    private val prices: Prices
) : PoolingMarketProvider() {

    val factoryAddress = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f"

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val contract = PairFactoryContract(
                getBlockchainGateway(),
                factoryAddress
            )

        val allPairs = contract.allPairs()
        logger.info("Found ${allPairs.size} Uniswap V2 Pools")
        allPairs.parMap(concurrency = 12) {
            Either.catch {
                val token = getToken(it)
                val breakdown = fiftyFiftyBreakdown(
                    token.underlyingTokens[0],
                    token.underlyingTokens[1],
                    token.address
                )

                val marketsize = breakdown.sumOf {
                    prices.calculatePrice(
                        GetPriceCommand(
                            it.token.address,
                            getNetwork(),
                            it.reserve.asEth(it.token.decimals)
                        )
                    )
                }

                if (marketsize > 0) {
                    create(
                        name = "Uniswap V2 ${token.symbol} Pool",
                        identifier = it,
                        address = it,
                        symbol = token.underlyingTokens[0].symbol + "/" + token.underlyingTokens[1].symbol,
                        totalSupply = refreshable {
                            token.totalSupply.asEth(18)
                        }
                    )
                } else {
                    throw IllegalArgumentException("marketsize too low")
                }
            }.mapLeft {
                logger.debug("Error creating Uniswap V2 Pool: {}", it.message)
            }.map {
                send(it)
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