package io.defitrack.protocol.application.hop.farming

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.utils.refreshable
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.hop.HopService
import io.defitrack.protocol.hop.contract.HopStakingRewardContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

abstract class HopFarmingMarketProvider(
    private val hopService: HopService,
) : FarmingMarketProvider() {

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        hopService.getStakingRewardsFromJson(getNetwork())
            .parMapNotNull(concurrency = 8) { stakingReward ->
                toStakingMarket(stakingReward)
            }.forEach {
                send(it)
            }
    }


    private suspend fun toStakingMarket(stakingReward: String): FarmingMarket {
        val contract = getStakingContract(stakingReward)

        val stakedToken = getToken(contract.stakingToken.await())
        val rewardToken = getToken(contract.rewardsToken.await())

        return create(
            identifier = contract.address,
            name = "${stakedToken.name} Staking Rewards",
            stakedToken = stakedToken,
            rewardToken = rewardToken,
            marketSize = refreshable {
                getMarketSize(stakedToken, contract.address)
            },
            positionFetcher = PositionFetcher(contract::balanceOfFunction),
            type = "hop.staking",
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    rewardToken,
                    contract::earnedFn,
                ),
                preparedTransaction = selfExecutingTransaction(contract::getRewardFn)
            ),
        )
    }

    private fun getStakingContract(stakingReward: String) = with(getBlockchainGateway()) {
        HopStakingRewardContract(
            stakingReward
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }
}