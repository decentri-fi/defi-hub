package io.defitrack.protocol.balancer.pooling

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
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
import java.math.BigInteger

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
        poolAddress: String,
    ): Option<PoolingMarket> {
        try {
            val poolContract = BalancerPoolContract(
                getBlockchainGateway(), poolAddress
            )
            val vault = BalancerVaultContract(
                getBlockchainGateway(),
                poolContract.vault.await()
            )

            val poolId = poolContract.getPoolId()

            val poolTokens = vault.getPoolTokens(poolId, poolAddress)

            if (poolTokens.all {
                    it.balance == BigInteger.ZERO
                }) {
                return None
            }

            val underlying = poolTokens.map { it ->
                getToken(it.token) to it.balance
            }

            return create(
                identifier = poolId,
                address = poolAddress,
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
                    calculateMarketSize(vault, poolId, poolAddress).also {
                        logger.debug(
                            "Market size for pool {} ({}) is {}",
                            poolAddress,
                            underlying.joinToString("/") { it.first.symbol },
                            it
                        )
                    }
                },
                positionFetcher = defaultPositionFetcher(poolAddress),
                totalSupply = refreshable {
                    getToken(poolAddress).totalDecimalSupply()
                }
            ).some()
        } catch (e: Exception) {
            logger.error("Error creating market for pool $poolAddress:  ex: {}", e.message)
            return None
        }
    }

    private suspend fun BalancerPoolingMarketProvider.calculateMarketSize(
        vault: BalancerVaultContract,
        poolId: String,
        poolAddress: String
    ): BigDecimal {
        val poolInfo = vault.getPoolTokens(poolId, poolAddress)

        val tokens = poolInfo.map { it ->
            getToken(it.token) to it.balance
        }

        return tokens.sumOf {
            getPriceResource().calculatePrice(
                PriceRequest(
                    it.first.address,
                    getNetwork(),
                    it.second.asEth(it.first.decimals)
                )
            )
        }.toBigDecimal()
    }

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }
}