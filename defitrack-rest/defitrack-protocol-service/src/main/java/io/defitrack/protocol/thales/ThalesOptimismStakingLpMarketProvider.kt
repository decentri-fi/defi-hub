package io.defitrack.protocol.thales

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.THALES)
class ThalesOptimismStakingLpMarketProvider : FarmingMarketProvider() {

    val stakingThales = "0x31a20e5b7b1b067705419d57ab4f72e81cc1f6bf"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val stakingThalesContract = ThalesLpStakingContract(
            getBlockchainGateway(),
            stakingThales
        )

        val stakedToken = getToken(stakingThalesContract.stakingToken.await())
        val rewardsToken = getToken(stakingThalesContract.rewardsToken.await())
        val secondRewardsToken = getToken(stakingThalesContract.secondRewardsToken.await())


        return listOf(
            create(
                name = "Thales LP Staking",
                identifier = stakingThales,
                stakedToken = stakedToken,
                rewardTokens = listOf(rewardsToken, secondRewardsToken),
                positionFetcher = PositionFetcher(
                    stakingThalesContract::stakedBalanceOfFn,
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    listOf(
                        Reward(
                            rewardsToken,
                            stakingThalesContract::getRewardsAvailableFn,
                        ) { res, _ ->
                            res[0].value as BigInteger
                        },
                        Reward(
                            secondRewardsToken,
                            stakingThalesContract::getRewardsAvailableFn
                        ) { res, _ ->
                            res[1].value as BigInteger
                        },
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
        return Network.OPTIMISM
    }
}