package io.defitrack.protocol.mycelium

import arrow.core.nonEmptyListOf
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.exit.ExitPositionCommand
import io.defitrack.exit.ExitPositionPreparer
import io.defitrack.exit.ExitPositionPreparer.Companion.defaultExitPositionProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.MYCELIUM)
class TcrRewardsProvider : FarmingMarketProvider() {

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = TcrStakingRewards(
            getBlockchainGateway(), "0xa18413dc5506a91138e0604c283e36b021b8849b"
        )


        val stakingToken = getToken(contract.stakingToken.await())
        val rewardToken = getToken(contract.rewardsToken.await())

        send(
            create(
                "Tcr Staking Rewards",
                identifier = "tcr staking rewards",
                stakedToken = stakingToken,
                rewardTokens = nonEmptyListOf(rewardToken),
                positionFetcher = defaultPositionFetcher(contract.address),
                exitPositionPreparer = defaultExitPositionProvider(getNetwork(), contract::exitFn)
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.MYCELIUM
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}