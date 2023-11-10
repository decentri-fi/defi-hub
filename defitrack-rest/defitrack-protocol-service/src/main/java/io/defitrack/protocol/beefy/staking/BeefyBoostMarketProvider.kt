package io.defitrack.protocol.beefy.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.exit.ExitPositionCommand
import io.defitrack.exit.ExitPositionPreparer
import io.defitrack.exit.ExitPositionPreparer.Companion.defaultExitPositionProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.contract.`BeefyLaunchPoolContract`
import io.defitrack.protocol.beefy.domain.BeefyLaunchPool
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

abstract class BeefyBoostMarketProvider(
    private val launchpools: MutableList<BeefyLaunchPool>
) : FarmingMarketProvider() {

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        launchpools.map {

            val contract = BeefyLaunchPoolContract(getBlockchainGateway(), it.earnContractAddress)
            val want = getToken(contract.stakedToken.await()) //should be fetched from farms
            val reward = getToken(it.earnedTokenAddress)


            create(
                name = want.name + " Boost",
                identifier = it.id,
                stakedToken = want,
                rewardToken = reward,
                rewardsFinished = it.status == "eol",
                metadata = mapOf("type" to "boost"),
                positionFetcher = defaultPositionFetcher(contract.address),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        reward,
                        contract.address,
                        contract::rewards
                    ),
                    preparedTransaction = selfExecutingTransaction(contract::getRewardfn)
                ),
                exitPositionPreparer = defaultExitPositionProvider(
                    getNetwork(),
                    contract::getRewardfn
                )
            )
        }.forEach {
            send(it)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BEEFY
    }

    override fun order(): Int {
        return 2
    }
}