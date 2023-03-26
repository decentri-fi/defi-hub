package io.defitrack.protocol.balancer.staking

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.protocol.balancer.graph.BalancerGaugeProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

abstract class BalancerGaugeFarmingMarketProvider(
    private val balancerGaugeGraphProvider: BalancerGaugeProvider
) : FarmingMarketProvider() {

    val balancerGaugeContractAbi by lazy {
        runBlocking {
            getAbi("balancer/gauge.json")
        }
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        balancerGaugeGraphProvider.getGauges().map {
            async {
                try {
                    val stakedToken = getToken(it.poolAddress)
                    val gauge = BalancerGaugeContract(
                        getBlockchainGateway(),
                        balancerGaugeContractAbi,
                        it.id
                    )

                    create(
                        identifier = it.id,
                        name = stakedToken.underlyingTokens.joinToString("/") {
                            it.symbol
                        } + " Gauge",
                        stakedToken = stakedToken.toFungibleToken(),
                        rewardTokens = getRewardTokens(
                            gauge
                        ).map { reward ->
                            reward.toFungibleToken()
                        },
                        vaultType = "balancerGauge",
                        balanceFetcher = PositionFetcher(
                            gauge.address,
                            { user -> gauge.balanceOfMethod(user) }
                        ),
                        farmType = ContractType.STAKING,
                        metadata = mapOf("address" to it.id),
                        exitPositionPreparer = prepareExit {
                            PreparedExit(
                                function = gauge.exitPosition(it.amount),
                                to = gauge.address,
                            )
                        }
                    )
                } catch (ex: Exception) {
                    logger.error(
                        "Error while fetching balancer gauge: ${ex.message}"
                    )
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun getRewardTokens(balancerGaugeContract: BalancerGaugeContract): List<TokenInformationVO> {
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


}