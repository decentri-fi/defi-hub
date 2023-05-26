package io.defitrack.protocol.balancer.staking

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.protocol.balancer.contract.BalancerLiquidityGaugeFactoryContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {

        val pools = poolingMarketProvider.getMarkets()

        pools.map { pool ->
            async {
                throttled {
                    try {
                        val gauge = factory.getPoolGauge(pool.address)

                        if (gauge == "0x0000000000000000000000000000000000000000") {
                            return@throttled null
                        }

                        val stakedToken = getToken(pool.address)
                        val gaugecontract = BalancerGaugeContract(
                            getBlockchainGateway(),
                            gauge
                        )

                        create(
                            identifier = pool.id,
                            name = stakedToken.underlyingTokens.joinToString("/") {
                                it.symbol
                            } + " Gauge",
                            stakedToken = stakedToken.toFungibleToken(),
                            rewardTokens = getRewardTokens(
                                gaugecontract
                            ).map { reward ->
                                reward.toFungibleToken()
                            },
                            vaultType = "balancerGauge",
                            balanceFetcher = PositionFetcher(
                                gaugecontract.address,
                                { user -> balanceOfFunction(user) }
                            ),
                            farmType = ContractType.STAKING,
                            metadata = mapOf("address" to pool.id),
                            exitPositionPreparer = prepareExit {
                                PreparedExit(
                                    function = gaugecontract.exitPosition(it.amount),
                                    to = gaugecontract.address,
                                )
                            }
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        logger.error(
                            "Error while fetching balancer gauge: ${ex.message}"
                        )
                        null
                    }
                }
            }
        }
    }.awaitAll().filterNotNull()


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