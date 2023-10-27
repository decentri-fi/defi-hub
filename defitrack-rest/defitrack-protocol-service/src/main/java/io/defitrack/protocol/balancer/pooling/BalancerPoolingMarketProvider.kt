package io.defitrack.protocol.balancer.pooling

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.BulkConstantResolver
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerPoolContract
import io.defitrack.protocol.balancer.contract.BalancerService
import io.defitrack.protocol.balancer.contract.BalancerVaultContract
import io.defitrack.protocol.balancer.pooling.history.BalancerPoolingHistoryProvider
import io.defitrack.protocol.spark.PoolContract
import io.defitrack.token.FungibleToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.math.BigInteger

abstract class BalancerPoolingMarketProvider(
    private val balancerService: BalancerService,
) : PoolingMarketProvider() {

    @Autowired
    private lateinit var bulkConstantResolver: BulkConstantResolver

    @Autowired
    private lateinit var balancerPoolingHistoryProvider: BalancerPoolingHistoryProvider

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val poolingContracts = balancerService.getPools(getNetwork())
            .map {
                BalancerPoolContract(
                    getBlockchainGateway(), it
                )
            }

        bulkConstantResolver.resolve(poolingContracts)

        poolingContracts
            .parMapNotNull(concurrency = 8) { pool ->
                createMarket(pool)
            }.forEach {
                it.onSome { send(it) }
            }
    }


    private suspend fun createMarket(
        poolContract: BalancerPoolContract,
    ): Option<PoolingMarket> {
        try {

            val poolAddress = poolContract.address
            val pool = getToken(poolAddress)

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

            val underlyingTokens = poolTokens.map {
                getToken(it.token)
            }

            return create(
                identifier = poolId,
                address = poolAddress,
                name = "${
                    underlyingTokens.joinToString("/", transform = TokenInformationVO::symbol)
                } Pool",
                tokens = underlyingTokens,
                symbol = underlyingTokens.joinToString("/", transform = TokenInformationVO::symbol),
                metadata = mapOf("poolId" to poolId),
                marketSize = refreshable {
                    calculateMarketSize(vault, poolId, poolAddress)
                },
                positionFetcher = defaultPositionFetcher(poolAddress),
                totalSupply = refreshable(pool.totalDecimalSupply()) {
                    getToken(poolAddress).totalDecimalSupply()
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

    private suspend fun BalancerPoolingMarketProvider.calculateMarketSize(
        vault: BalancerVaultContract,
        poolId: String,
        poolAddress: String
    ): BigDecimal {
        val poolInfo = vault.getPoolTokens(poolId, poolAddress)

        val tokens = poolInfo.map {
            TokenWithBalance(
                getToken(it.token), it.balance
            )
        }

        return tokens.sumOf { tokenWithBalance ->
            getPriceResource().calculatePrice(
                PriceRequest(
                    tokenWithBalance.token.address,
                    getNetwork(),
                    tokenWithBalance.balance.asEth(tokenWithBalance.token.decimals)
                )
            )
        }.toBigDecimal()
    }

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }
}