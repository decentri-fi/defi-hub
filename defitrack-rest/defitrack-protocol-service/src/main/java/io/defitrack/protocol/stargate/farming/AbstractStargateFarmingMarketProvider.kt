package io.defitrack.protocol.stargate.farming

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.stargate.StargateService
import io.defitrack.protocol.stargate.contract.LPStakingContract
import io.defitrack.transaction.PreparedTransaction

abstract class AbstractStargateFarmingMarketProvider(
    private val stargateService: StargateService,
    private val pendingFunctionName: String = "pendingStargate",
    private val emissionTokenName: String = "stargate"
) : FarmingMarketProvider() {


    override fun getProtocol(): Protocol {
        return Protocol.STARGATE
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val lpStakingContract = LPStakingContract(
            getBlockchainGateway(),
            stargateService.getLpFarm(),
            pendingFunctionName,
            emissionTokenName
        )
        val stargate = getToken(lpStakingContract.emissionToken())

        return lpStakingContract.poolInfos().mapIndexed { index, info ->
            val stakedToken = getToken(info.lpToken)
            val rewardToken = stargate

            create(
                identifier = "${lpStakingContract.address}-$index",
                name = "Stargate ${stakedToken.name} Reward",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(rewardToken.toFungibleToken()),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token =  rewardToken.toFungibleToken(),
                        contractAddress = lpStakingContract.address,
                        getRewardFunction = { user ->
                            lpStakingContract.pendingFn(index, user)
                        }
                    ),
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            getNetwork().toVO(),
                            lpStakingContract.claimFn(index),
                            lpStakingContract.address,
                            user
                        )
                    }
                ),
                positionFetcher = PositionFetcher(
                    lpStakingContract.address,
                    { user: String -> lpStakingContract.userInfo(index, user) },
                )
            )
        }
    }
}