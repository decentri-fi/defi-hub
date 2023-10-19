package io.defitrack.protocol.thales

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
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
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(
                    rewardsToken.toFungibleToken(), secondRewardsToken.toFungibleToken()
                ),
                positionFetcher = PositionFetcher(
                    stakingThales,
                    stakingThalesContract::stakedBalanceOfFn,
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    listOf(
                        Reward(
                            rewardsToken.toFungibleToken(),
                            stakingThales,
                            stakingThalesContract::getRewardsAvailableFn,
                        ) { res, _ ->
                            res[0].value as BigInteger
                        },
                        Reward(
                            secondRewardsToken.toFungibleToken(),
                            stakingThales,
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