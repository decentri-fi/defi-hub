package io.defitrack.protocol.application.mycelium

import arrow.core.nonEmptyListOf
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mycelium.TcrStakingRewards
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.MYCELIUM)
class TcrRewardsProvider : FarmingMarketProvider() {

    context(BlockchainGateway)
    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = TcrStakingRewards("0xa18413dc5506a91138e0604c283e36b021b8849b")

        val stakingToken = getToken(contract.stakingToken.await())
        val rewardToken = getToken(contract.rewardsToken.await())

        send(
            create(
                "Tcr Staking Rewards",
                identifier = "tcr staking rewards",
                stakedToken = stakingToken,
                rewardToken = rewardToken,
                positionFetcher = defaultPositionFetcher(contract.address),
                type = "tcr.staking-rewards",
                exitPositionPreparer = prepareExit {
                    contract.exitFn()
                }
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