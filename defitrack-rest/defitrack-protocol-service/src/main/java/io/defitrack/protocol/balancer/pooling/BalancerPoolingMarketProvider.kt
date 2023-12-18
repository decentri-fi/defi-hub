package io.defitrack.protocol.balancer.pooling

import arrow.core.*
import arrow.core.raise.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.common.utils.toRefreshable
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
import java.math.BigDecimal
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

            it.second
                .parMapNotNull(concurrency = 8) { pool ->
                    createMarket(pool, vault)
                }.forEach {
                    it.onSome { send(it) }
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
            val poolTokens = vault.getPoolTokens(poolId, poolAddress)

            if (poolTokens.all {
                    it.balance == BigInteger.ZERO
                }) {
                return None
            }

            val underlyingTokens = poolTokens.map {
                getToken(it.token)
            }


            val tokens = poolTokens.map {
                TokenWithBalance(
                    getToken(it.token), it.balance
                )
            }

            val breakdown = refreshable {
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

            return create(
                identifier = poolId,
                address = poolAddress,
                name = "${
                    underlyingTokens.joinToString("/", transform = FungibleToken::symbol)
                } Pool",
                tokens = underlyingTokens,
                symbol = underlyingTokens.joinToString("/", transform = FungibleToken::symbol),
                metadata = mapOf("poolId" to poolId),
                breakdown = breakdown,
                marketSize = breakdown.map {
                    it.sumOf { share -> share.reserveUSD }
                },
                positionFetcher = defaultPositionFetcher(poolAddress),
                totalSupply = refreshable {
                    BigDecimal.ZERO
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