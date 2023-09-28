package io.defitrack.farming

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.claimable.Reward
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.StargateService
import io.defitrack.protocol.contract.LPStakingContract
import io.defitrack.transaction.PreparedTransaction

abstract class StargateFarmingMarketProvider(
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
                vaultType = "stargate-lp-staking",
                farmType = ContractType.LIQUIDITY_MINING,
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
                balanceFetcher = PositionFetcher(
                    lpStakingContract.address,
                    { user: String -> lpStakingContract.userInfo(index, user) },
                )
            )
        }
    }
}