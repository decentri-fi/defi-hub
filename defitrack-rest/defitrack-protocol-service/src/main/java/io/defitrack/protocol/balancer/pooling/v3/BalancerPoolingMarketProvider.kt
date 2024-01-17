package io.defitrack.protocol.balancer.pooling.v3

import arrow.core.*
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.erc20.FungibleToken
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerPoolContract
import io.defitrack.protocol.balancer.contract.BalancerService
import io.defitrack.protocol.balancer.contract.BalancerVaultContract
import io.defitrack.protocol.balancer.pooling.history.BalancerPoolingHistoryProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigInteger

abstract class BalancerPoolingMarketProvider(
    private val balancerService: BalancerService,
    private val balancerPoolingHistoryProvider: BalancerPoolingHistoryProvider
) : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val poolsPerVault = resolve(
            balancerService.getPools(getNetwork())
                .map {
                    BalancerPoolContract(
                        getBlockchainGateway(), it
                    )
                }
        ).groupBy { it.vault.await() }.map {
            BalancerVaultContract(
                getBlockchainGateway(),
                it.key
            ) to it.value
        }

        poolsPerVault.forEach {
            val vault = it.first
            vault.cachePoolTokens(
                it.second.map { pool ->
                    pool.getPoolId()
                }
            )

            it.second.parMapNotNull(concurrency = 8) { pool ->
                createMarket(pool, vault)
            }.mapNotNull {
                it.getOrNull()
            }.forEach {
                send(it)
            }
        }
    }


    private suspend fun createMarket(
        poolContract: BalancerPoolContract,
        vault: BalancerVaultContract
    ): Option<PoolingMarket> {
        try {

            val poolAddress = poolContract.address

            val poolId = poolContract.getPoolId()

            val breakdown = refreshable {
                val poolTokens = vault.getPoolTokens(poolId, poolAddress)

                val tokens = poolTokens.map {
                    TokenWithBalance(
                        getToken(it.token), it.balance
                    )
                }
                tokens.map {
                    PoolingMarketTokenShare(
                        it.token,
                        it.balance,
                        getPriceResource().calculatePrice(
                            PriceRequest(
                                it.token.address,
                                getNetwork(),
                                it.balance.asEth(it.token.decimals)
                            )
                        ).toBigDecimal()
                    )
                }
            }

            val underlying = breakdown.get().map {
                it.token
            }

            return create(
                identifier = poolId,
                address = poolAddress,
                name = "${
                    underlying.joinToString("/", transform = FungibleToken::symbol)
                } Pool",
                symbol = underlying.joinToString("/", transform = FungibleToken::symbol),
                metadata = mapOf("poolId" to poolId),
                breakdown = breakdown,
                positionFetcher = defaultPositionFetcher(poolAddress),
                totalSupply = refreshable {
                    Either.catch {
                        poolContract.actualSupply.await().asEth(
                            getToken(poolContract.address).decimals
                        )
                    }.getOrElse {
                        getToken(poolContract.address).totalDecimalSupply()
                    }
                },
                historicEventExtractor = balancerPoolingHistoryProvider.historicEventExtractor(
                    poolId, getNetwork()
                )
            ).some()
        } catch (e: Exception) {
            logger.error("Error creating market for pool ${poolContract.address}:  ex: {}", e.message)
            return None
        }
    }

    data class TokenWithBalance(
        val token: FungibleToken,
        val balance: BigInteger
    )

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }
}