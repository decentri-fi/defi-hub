package io.defitrack.protocol.application.curve.staking

import arrow.fx.coroutines.parMap
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.CurveL2GaugeContract
import io.defitrack.protocol.crv.contract.CurvePolygonGaugeControllerContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction

abstract class CurveGaugeFarmingMarketProvider(
    private val gaugeControllerAddress: String
) : FarmingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val gaugeController = CurvePolygonGaugeControllerContract(gaugeControllerAddress)

        return gaugeController
            .getGaugeAddresses()
            .parMap(concurrency = 12) { gauge ->
                val contract = with(getBlockchainGateway()) {
                    CurveL2GaugeContract(
                        gauge
                    )
                }

                val rewardTokens = contract.rewardTokens().map { getToken(it) }
                val stakedToken = getToken(contract.lpToken())

                create(
                    identifier = gauge,
                    name = stakedToken.name + " Gauge",
                    stakedToken = stakedToken,
                    rewardTokens = rewardTokens,
                    marketSize = refreshable {
                        getMarketSize(stakedToken, gauge)
                    },
                    type = "curve.gauge",
                    positionFetcher = PositionFetcher(
                        contract::balanceOfFunction
                    ),
                    claimableRewardFetcher = rewardTokens.takeIf { it.isNotEmpty() }?.let { rewards ->
                        ClaimableRewardFetcher(
                            rewards.map { reward ->
                                Reward(
                                    token = reward,
                                    getRewardFunction = contract.getClaimableRewardFunction(
                                        reward.address
                                    )
                                )
                            },
                            preparedTransaction = selfExecutingTransaction(contract::getClaimRewardsFunction)
                        )
                    }
                )
            }
    }
}