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
        poolingMarketProvider.getMarkets()
            .parMapNotNull(concurrency = 12) { pool ->
                getMarket(pool).mapLeft {
                    logger.error("error getting market for ${pool.address}", it)
                }.map {
                    it.getOrNull()
                }.getOrNull()
            }.forEach {
                send(it)
            }
    }

    private suspend fun getMarket(pool: PoolingMarket): Either<Throwable, Option<FarmingMarket>> =
        with(getBlockchainGateway()) {
            return catch {
                val gauge = factory.getPoolGauge(pool.address)

                if (gauge == "0x0000000000000000000000000000000000000000") {
                    logger.debug("no gauge for ${pool.address}")
                    None
                } else {
                    val stakedToken = getToken(pool.address)
                    val gaugecontract = BalancerGaugeContract(
                        gauge
                    )

                    val rewardTokens = gaugecontract.getRewardTokens().map { reward ->
                        getToken(reward)
                    }

                    create(
                        identifier = pool.id,
                        name = stakedToken.underlyingTokens.joinToString("/") {
                            it.symbol
                        } + " Gauge",
                        stakedToken = stakedToken,
                        rewardTokens = rewardTokens,
                        positionFetcher = PositionFetcher(
                            gaugecontract::balanceOfFunction
                        ),
                        metadata = mapOf("address" to pool.address),
                        internalMetadata = mapOf(
                            "contract" to gauge,
                        ),
                        claimableRewardFetcher = ClaimableRewardFetcher(
                            rewards = rewardTokens.map {
                                Reward(
                                    token = it,
                                    getRewardFunction = gaugecontract.getClaimableRewardFunction(it.address)
                                )
                            },
                            preparedTransaction = selfExecutingTransaction(gaugecontract::getClaimRewardsFunction)
                        ),
                        exitPositionPreparer = prepareExit {
                            gaugecontract.exitPosition(it.amount)
                        }
                    ).some()
                }
            }
        }


    override fun getNetwork(): Network {
        return poolingMarketProvider.getNetwork()
    }
}