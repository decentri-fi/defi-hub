package io.defitrack.protocol.balancer.pooling

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerPoolContract
import io.defitrack.protocol.balancer.contract.BalancerService
import io.defitrack.protocol.balancer.contract.BalancerVaultContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigDecimal
import kotlin.coroutines.EmptyCoroutineContext

abstract class BalancerPoolingMarketProvider(
    private val balancerService: BalancerService
) : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        balancerService.getPools(getNetwork()).parMapNotNull(concurrency = 8) { pool ->
            createMarket(pool)
        }.forEach {
            it.onSome { send(it) }
        }
    }

    private suspend fun createMarket(
        pool: String,
    ): Option<PoolingMarket> {
        try {
            val poolContract = BalancerPoolContract(
                getBlockchainGateway(), pool
            )
            val vault = BalancerVaultContract(
                getBlockchainGateway(),
                poolContract.vault.await()
            )

            val poolId = poolContract.getPoolId()

            val poolTokens = vault.getPoolTokens(poolId)
            val underlying = poolTokens.tokens.mapIndexed { index, it ->
                getToken(it) to poolTokens.balances[index]
            }.filter {
                it.first.address.lowercase() != pool.lowercase()
            }

            return create(
                identifier = poolId,
                address = pool,
                name = "${
                    underlying.joinToString("/") {
                        it.first.symbol
                    }
                } Pool",
                tokens = underlying.map {
                    it.first.toFungibleToken()
                },
                symbol = underlying.joinToString("/") {
                    it.first.symbol
                },
                metadata = mapOf(
                    "poolId" to poolId,
                ),
                marketSize = refreshable {
                    val poolInfo = vault.getPoolTokens(poolId)

                    val tokens = poolInfo.tokens.mapIndexed { index, address ->
                        val token = getToken(address)
                        val balance = poolInfo.balances[index]
                        token to balance
                    }.filter {
                        it.first.address != pool
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
                positionFetcher = defaultPositionFetcher(pool),
                totalSupply = refreshable {
                    getToken(pool).totalDecimalSupply()
                }
            ).some()
        } catch (e: Exception) {
            logger.error("Error creating market for pool $pool:  ex: {}", e.message)
            return None
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }
}