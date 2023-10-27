package io.defitrack.protocol.stargate.farming

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.stargate.StargateService
import io.defitrack.protocol.stargate.contract.LPStakingContract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction

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

            create(
                identifier = "${lpStakingContract.address}-$index",
                name = "Stargate ${stakedToken.name} Reward",
                stakedToken = stakedToken,
                rewardToken = stargate,
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = stargate.toFungibleToken(),
                        contractAddress = lpStakingContract.address,
                        getRewardFunction = lpStakingContract.pendingFn(index)
                    ),
                    preparedTransaction = selfExecutingTransaction(lpStakingContract.claimFn(index))
                ),
                positionFetcher = PositionFetcher(
                    lpStakingContract.address,
                    lpStakingContract.userInfo(index),
                )
            )
        }
    }
}