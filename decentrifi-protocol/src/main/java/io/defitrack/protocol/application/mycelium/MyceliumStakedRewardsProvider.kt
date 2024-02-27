package io.defitrack.protocol.application.mycelium

import arrow.core.nonEmptyListOf
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mycelium.RewardRouter02Contract
import io.defitrack.protocol.mycelium.RewardTrackerContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.MYCELIUM)
class MyceliumStakedRewardsProvider : FarmingMarketProvider() {
    val myCeliumStakingContractAddress = "0xd98d8e458f7ad22dd3c1d7a8b35c74005eb52b0b"

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = RewardRouter02Contract(
            getBlockchainGateway(), myCeliumStakingContractAddress
        )

        val mlp = getToken(contract.mlp.await())

        val mlpRewards = listOf(
            feeMlpMarket(contract),
            stakedMlpTracker(contract),
        )

        val stakedMlpTracker = rewardTrackerContract(contract)

        send(
            create(
                "MLP Staked",
                identifier = "staked-mlp",
                stakedToken = mlp,
                rewardTokens = mlpRewards.map { it.token },
                positionFetcher = PositionFetcher(
                    stakedMlpTracker::balanceOfFunction,
                ),
                claimableRewardFetchers = nonEmptyListOf(
                    ClaimableRewardFetcher(
                        mlpRewards,
                        selfExecutingTransaction(contract::claim)
                    )
                )
            )
        )
    }

    private suspend fun rewardTrackerContract(contract: RewardRouter02Contract) = with(getBlockchainGateway()) {
        RewardTrackerContract(contract.stakedMlpTracker.await())
    }


    suspend fun stakedMlpTracker(contract: RewardRouter02Contract): Reward = with(getBlockchainGateway()) {
        val rewardTracker = RewardTrackerContract(contract.stakedMlpTracker.await())
        val rewardToken = getToken(rewardTracker.rewardToken.await())

        return Reward(
            rewardToken,
            rewardTracker::claimable
        )
    }

    suspend fun feeMlpMarket(contract: RewardRouter02Contract): Reward = with(getBlockchainGateway()) {
        val rewardTracker = RewardTrackerContract(contract.feeMlpTracker.await())
        val rewardToken = getToken(rewardTracker.rewardToken.await())

        return Reward(
            rewardToken,
            rewardTracker::claimable
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.MYCELIUM
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}