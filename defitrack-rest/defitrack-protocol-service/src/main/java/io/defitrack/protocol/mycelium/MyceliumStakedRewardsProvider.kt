package io.defitrack.protocol.mycelium

import arrow.core.nonEmptyListOf
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
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

        val stakedMlpTracker = RewardTrackerContract(getBlockchainGateway(), contract.stakedMlpTracker.await())

        send(
            create(
                "MLP Staked",
                identifier = "staked-mlp",
                stakedToken = mlp,
                rewardTokens = mlpRewards.map { it.token },
                positionFetcher = PositionFetcher(
                    stakedMlpTracker.address,
                    ERC20Contract::balanceOfFunction
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


    suspend fun stakedMlpTracker(contract: RewardRouter02Contract): Reward {
        val rewardTracker = RewardTrackerContract(getBlockchainGateway(), contract.stakedMlpTracker.await())
        val rewardToken = getToken(rewardTracker.rewardToken.await())

        return Reward(
            rewardToken,
            rewardTracker.address,
            rewardTracker::claimable
        )
    }

    suspend fun feeMlpMarket(contract: RewardRouter02Contract): Reward {
        val rewardTracker = RewardTrackerContract(getBlockchainGateway(), contract.feeMlpTracker.await())
        val rewardToken = getToken(rewardTracker.rewardToken.await())

        return Reward(
            rewardToken,
            rewardTracker.address,
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