package io.defitrack.protocol.curve.staking

import arrow.fx.coroutines.parMap
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.utils.Refreshable
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.CurveL2GaugeContract
import io.defitrack.protocol.crv.contract.CurvePolygonGaugeControllerContract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction

abstract class CurveGaugeFarmingMarketProvider(
    private val gaugeControllerAddress: String
) : FarmingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val gaugeController = CurvePolygonGaugeControllerContract(
            blockchainGateway = getBlockchainGateway(),
            address = gaugeControllerAddress
        )

        return gaugeController
            .getGaugeAddresses()
            .parMap(concurrency = 12) { gauge ->
                val contract = CurveL2GaugeContract(
                    getBlockchainGateway(),
                    gauge
                )
                val rewardTokens = contract.rewardTokens()
                    .filter {
                        it != "0x0000000000000000000000000000000000000000"
                    }
                    .map { getToken(it) }

                val stakedToken = getToken(contract.lpToken())

                create(
                    identifier = gauge,
                    name = stakedToken.name + " Gauge",
                    stakedToken = stakedToken,
                    rewardTokens = rewardTokens,
                    marketSize = Refreshable.refreshable {
                        marketSizeService.getMarketSize(
                            stakedToken, gauge, getNetwork()
                        ).usdAmount
                    },
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