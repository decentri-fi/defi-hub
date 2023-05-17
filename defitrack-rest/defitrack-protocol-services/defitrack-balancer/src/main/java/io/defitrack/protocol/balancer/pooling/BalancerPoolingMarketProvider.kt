package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerPoolGraphProvider
import io.defitrack.protocol.balancer.Pool
import io.defitrack.protocol.balancer.contract.BalancerPoolContract
import io.defitrack.protocol.balancer.contract.BalancerVaultContract
import io.defitrack.token.TokenType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

abstract class BalancerPoolingMarketProvider(
    private val balancerPoolGraphProvider: BalancerPoolGraphProvider
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        balancerPoolGraphProvider.getPools().forEach {
            launch {
                throttled {
                    try {
                        createMarket(it)?.let {
                            send(it)
                        }
                    } catch (ex: Exception) {
                        logger.error("Unable to get pool information for ${it.id}", ex)
                    }
                }
            }
        }
    }

    private suspend fun createMarket(it: Pool): PoolingMarket? {
        return if (it.totalLiquidity > BigDecimal.valueOf(100000)) {
            val poolContract = BalancerPoolContract(
                getBlockchainGateway(), it.address
            )
            val vault = BalancerVaultContract(
                getBlockchainGateway(),
                poolContract.getVault()
            )

            create(
                identifier = it.id,
                address = it.address,
                name = "${
                    it.tokens.joinToString("/") {
                        it.symbol
                    }
                } Pool",
                tokens = emptyList(),
                symbol = it.symbol,
                apr = BigDecimal.ZERO,
                marketSize = Refreshable.refreshable {
                    val poolInfo = vault.getPoolTokens(it.id)

                    val tokens = poolInfo.tokens.mapIndexed { index, address ->
                        val token = getToken(address)
                        val balance = poolInfo.balances[index]
                        token to balance
                    }


                    tokens.sumOf {
                        getPriceResource().calculatePrice(
                            PriceRequest(
                                it.first.address,
                                getNetwork(),
                                it.second.asEth(it.first.decimals)
                            )
                        )
                    }.toBigDecimal()
                },
                tokenType = TokenType.BALANCER,
                positionFetcher = defaultPositionFetcher(it.address),
                totalSupply = Refreshable.refreshable {
                    getToken(it.address).totalDecimalSupply()
                }
            )
        } else {
            null
        }
    }
}