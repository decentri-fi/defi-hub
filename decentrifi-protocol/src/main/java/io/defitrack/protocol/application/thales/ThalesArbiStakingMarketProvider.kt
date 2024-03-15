package io.defitrack.protocol.application.thales

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.thales.StakingThalesContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.THALES)
class ThalesArbiStakingMarketProvider : FarmingMarketProvider() {

    val stakingThales = "0x160ca569999601bca06109d42d561d85d6bb4b57"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val stakingThalesContract = StakingThalesContract(
            getBlockchainGateway(),
            stakingThales
        )

        val stakedToken = getToken(stakingThalesContract.stakingToken.await())


        return listOf(
            create(
                name = "Thales Staking",
                identifier = stakingThales,
                stakedToken = stakedToken,
                rewardToken = stakedToken,
                positionFetcher = PositionFetcher(
                    stakingThalesContract::stakedBalanceOfFn,
                ),
                type = "thales.staking",
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        stakedToken,
                        stakingThalesContract::getRewardsAvailableFn,
                    ),
                    selfExecutingTransaction(stakingThalesContract::claimRewardFn)
                ),
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.THALES
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}