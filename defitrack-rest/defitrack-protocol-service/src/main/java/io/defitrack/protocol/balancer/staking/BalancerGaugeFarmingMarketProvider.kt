package io.defitrack.protocol.balancer.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.protocol.balancer.contract.BalancerLiquidityGaugeFactoryContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

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

        pools.forEach { pool ->
            launch {
                throttled {
                    try {
                        val gauge = factory.getPoolGauge(pool.address)

                        if (gauge == "0x0000000000000000000000000000000000000000") {
                            logger.debug("no gauge for ${pool.address}")
                        } else {
                            val stakedToken = getToken(pool.address)
                            val gaugecontract = BalancerGaugeContract(
                                getBlockchainGateway(),
                                gauge
                            )

                            val rewardTokens = getRewardTokens(gaugecontract).map { reward ->
                                reward.toFungibleToken()
                            }

                            send(
                                create(
                                    identifier = pool.id,
                                    name = stakedToken.underlyingTokens.joinToString("/") {
                                        it.symbol
                                    } + " Gauge",
                                    stakedToken = stakedToken.toFungibleToken(),
                                    rewardTokens = rewardTokens,
                                    positionFetcher = PositionFetcher(
                                        gaugecontract.address,
                                        { user -> balanceOfFunction(user) }
                                    ),
                                    metadata = mapOf("address" to pool.address),
                                    internalMetadata = mapOf(
                                        "contract" to gauge,
                                    ),
                                    claimableRewardFetcher = ClaimableRewardFetcher(
                                        rewards = rewardTokens.map {
                                            Reward(
                                                token = it,
                                                contractAddress = gaugecontract.address,
                                                getRewardFunction = { user ->
                                                    gaugecontract.getClaimableRewardFunction(user, it.address)
                                                }
                                            )
                                        },
                                        preparedTransaction = selfExecutingTransaction(gaugecontract::getClaimRewardsFunction)
                                    ),
                                    exitPositionPreparer = prepareExit {
                                        PreparedExit(
                                            function = gaugecontract.exitPosition(it.amount),
                                            to = gaugecontract.address,
                                        )
                                    }
                                )
                            )
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        logger.error(
                            "Error while fetching balancer gauge: ${ex.message}"
                        )
                    }
                }
            }
        }
    }


    private suspend fun getRewardTokens(balancerGaugeContract: BalancerGaugeContract): List<TokenInformationVO> {
        return (0..3).mapNotNull {
            try {
                val rewardToken = balancerGaugeContract.getRewardToken(it)
                if (rewardToken != "0x0000000000000000000000000000000000000000") {
                    getToken(rewardToken)
                } else {
                    null
                }
            } catch (ex: Exception) {
                null
            }
        }
    }

    override fun getNetwork(): Network {
        return poolingMarketProvider.getNetwork()
    }
}