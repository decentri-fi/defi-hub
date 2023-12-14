package io.defitrack.protocol.stargate.farming

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.stargate.StargateService
import io.defitrack.protocol.stargate.contract.LPStakingContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction

abstract class AbstractStargateLPStakingTimeMarketProvider(
    private val stargateService: StargateService,
    private val pendingFunctionName: String = "pendingEmissionToken",
    private val emissionTokenName: String = "eToken"
) : FarmingMarketProvider() {


    override fun getProtocol(): Protocol {
        return Protocol.STARGATE
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val lpStakingContract = LPStakingContract(
            getBlockchainGateway(),
            stargateService.getLpStakingTimeFarm(),
            pendingFunctionName,
            emissionTokenName
        )
        val stargate = getToken(lpStakingContract.emissionToken())

        return lpStakingContract.poolInfos().mapIndexed { index, info ->
            val stakedToken = getToken(info.lpToken)

            create(
                identifier = "${lpStakingContract.address}-$index",
                name = "${stakedToken.name} LP Staking Time",
                stakedToken = stakedToken,
                rewardToken = stargate,
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = stargate,
                        getRewardFunction = lpStakingContract.pendingFn(index)
                    ),
                    preparedTransaction = selfExecutingTransaction(lpStakingContract.claimFn(index))
                ),
                positionFetcher = PositionFetcher(
                    lpStakingContract.userInfo(index),
                )
            )
        }
    }
}