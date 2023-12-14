package io.defitrack.protocol.thales

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.THALES)
class ThalesBaseStakingMarketProvider : FarmingMarketProvider() {

    val stakingThales = "0x84ab38e42d8da33b480762cca543eeca6135e040"

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
        return Network.BASE
    }
}