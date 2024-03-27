package io.defitrack.protocol.application.balancer.staking

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.protocol.balancer.contract.BalancerLiquidityGaugeFactoryContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

abstract class BalancerGaugeFarmingMarketProvider(
    private val poolingMarketProvider: PoolingMarketProvider,
    private val gaugeFactory: String,
) : FarmingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    val factory by lazy {
        BalancerLiquidityGaugeFactoryContract(
            getBlockchainGateway(),
            gaugeFactory
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val pools = poolingMarketProvider.getMarkets()
        val poolsWithGauges = pools.zip(
            factory.getPoolGauges(pools.map { it.address }).map { gauge ->
                if (gauge == "0x0000000000000000000000000000000000000000") {
                    null
                } else {
                    createContract {
                        BalancerGaugeContract(gauge)
                    }
                }
            }
        ).filter {
            it.second != null
        }

        poolsWithGauges.map {
            it.second!!
        }.resolve()

        poolsWithGauges
            .parMapNotNull(concurrency = 12) { (pool, gauge) ->
                getMarket(pool, gauge!!).mapLeft {
                    logger.error("error getting market for ${pool.address}", it)
                }.map {
                    it.getOrNull()
                }.getOrNull()
            }.forEach {
                send(it)
            }
    }

    private suspend fun getMarket(
        pool: PoolingMarket,
        gauge: BalancerGaugeContract
    ): Either<Throwable, Option<FarmingMarket>> =
        with(getBlockchainGateway()) {
            return catch {

                val stakedToken = getToken(pool.address)

                val rewardTokens = gauge.getRewardTokens().map { reward ->
                    getToken(reward)
                }

                create(
                    identifier = pool.id,
                    name =   "${pool.breakdown.get().joinToString("/") { 
                        it.token.symbol
                    }} Gauge",
                    stakedToken = stakedToken,
                    rewardTokens = rewardTokens,
                    positionFetcher = PositionFetcher(
                        gauge::balanceOfFunction
                    ),
                    metadata = mapOf("address" to pool.address),
                    internalMetadata = mapOf(
                        "contract" to gauge,
                    ),
                    claimableRewardFetcher = ClaimableRewardFetcher(
                        rewards = rewardTokens.map {
                            Reward(
                                token = it,
                                getRewardFunction = gauge.getClaimableRewardFunction(it.address)
                            )
                        },
                        preparedTransaction = selfExecutingTransaction(gauge::getClaimRewardsFunction)
                    ),
                    type = "balancer.gauge",
                    exitPositionPreparer = prepareExit {
                        gauge.exitPosition(it.amount)
                    }
                ).some()
            }
        }


    override fun getNetwork(): Network {
        return poolingMarketProvider.getNetwork()
    }
}