package io.defitrack.protocol.application.uniswap.v2

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.contract.LPTokenContract
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.port.output.PriceClient
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnNetwork(Network.ETHEREUM)
@ConditionalOnCompany(Company.UNISWAP)
@ConditionalOnProperty(value = ["uniswapv2.fresh.enabled"], havingValue = "true", matchIfMissing = false)
class UniswapV2EthereumPoolingMarketProvider(
    private val prices: PriceClient
) : PoolingMarketProvider() {

    val factoryAddress = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f"

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val contract = with(getBlockchainGateway()) { PairFactoryContract(factoryAddress) }

        val allPairs = contract.allPairs()
            .map {
                createContract {
                    LPTokenContract(it)
                }
            }.resolve()
        logger.info("Found ${allPairs.size} Uniswap V2 Pools")
        allPairs.parMap(concurrency = 12) {
            catch {
                val token = getToken(it.address)
                val token0 = getToken(it.token0.await())
                val token1 = getToken(it.token1.await())
                val breakdown = refreshable {
                    breakdownOf(
                        token.address,
                        token0,
                        token1,
                    )
                }

                val marketsize = breakdown.get().sumOf {
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
                        symbol = breakdown.get().joinToString("/") { it.token.symbol },
                        name = breakdown.get().joinToString("/") { it.token.name },
                        identifier = it.address,
                        address = it.address,
                        breakdown = breakdown,
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