package io.defitrack.protocol.application.balancer.pooling.v3

import arrow.core.*
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerPoolContract
import io.defitrack.protocol.balancer.contract.BalancerService
import io.defitrack.protocol.balancer.contract.BalancerVaultContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigInteger

abstract class BalancerPoolingMarketProvider(
    private val balancerService: BalancerService,
) : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val poolsPerVault = balancerService.getPools(getNetwork())
            .map {
                balancerPoolContract(it)
            }
            .resolve()
            .groupBy { it.vault.await() }.map {
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

    private fun balancerPoolContract(it: String) = with(getBlockchainGateway()) {
        BalancerPoolContract(it)
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
                    underlying.joinToString("/", transform = FungibleTokenInformation::symbol)
                } Pool",
                symbol = underlying.joinToString("/", transform = FungibleTokenInformation::symbol),
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
                }
            ).some()
        } catch (e: Exception) {
            logger.error("Error creating market for pool ${poolContract.address}:  ex: {}", e.message)
            return None
        }
    }

    data class TokenWithBalance(
        val token: FungibleTokenInformation,
        val balance: BigInteger
    )

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }
}