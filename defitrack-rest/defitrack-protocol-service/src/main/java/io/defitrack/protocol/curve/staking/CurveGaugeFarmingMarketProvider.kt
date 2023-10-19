package io.defitrack.protocol.curve.staking

import arrow.fx.coroutines.parMap
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.utils.Refreshable
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.CurveGaugeContract
import io.defitrack.protocol.crv.contract.CurvePolygonGaugeControllerContract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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
                val contract = CurveGaugeContract(
                    getBlockchainGateway(),
                    gauge
                )
                val rewardTokens = contract.rewardTokens()
                    .filter {
                        it != "0x0000000000000000000000000000000000000000"
                    }
                    .map {
                        getToken(it).toFungibleToken()
                    }

                val stakedToken = getToken(contract.lpToken())

                create(
                    identifier = gauge,
                    name = stakedToken.name + " Gauge",
                    stakedToken = stakedToken.toFungibleToken(),
                    rewardTokens = rewardTokens,
                    marketSize = Refreshable.refreshable {
                        marketSizeService.getMarketSize(
                            stakedToken.toFungibleToken(), gauge, getNetwork()
                        )
                    },
                    positionFetcher = PositionFetcher(
                        gauge,
                        { user ->
                            balanceOfFunction(user)
                        }
                    ),
                    claimableRewardFetcher = rewardTokens.takeIf { it.isNotEmpty() }?.let { rewards ->
                        ClaimableRewardFetcher(
                            rewards.map { reward ->
                                Reward(
                                    token = reward,
                                    contractAddress = contract.address,
                                    getRewardFunction = { user ->
                                        contract.getClaimableRewardFunction(
                                            user, rewardTokens.first().address
                                        )
                                    }
                                )
                            },
                            preparedTransaction = selfExecutingTransaction(contract::getClaimRewardsFunction)
                        )
                    }
                )
            }
    }
}