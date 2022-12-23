package io.defitrack.protocol.polycat.staking

import io.defitrack.apr.Reward
import io.defitrack.apr.StakedAsset
import io.defitrack.apr.StakingAprCalculator
import io.defitrack.price.PriceResource
import io.defitrack.protocol.polycat.contract.PolycatMasterChefContract
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import java.math.BigDecimal
import java.math.RoundingMode

class PolygcatStakingAprCalculator(
    private val erc20Resource: ERC20Resource,
    priceResource: PriceResource,
    private val chef: PolycatMasterChefContract,
    private val poolId: Int
) : StakingAprCalculator(priceResource) {


    suspend fun getNativeReward(): Reward {
        val poolInfo = chef.poolInfo(poolId)
        val allSushiPerSecond = chef.rewardPerBlock()
        val poolBlockRewards = allSushiPerSecond.times(poolInfo.allocPoint).divide(chef.totalAllocPoint())
        return Reward(
            address = chef.rewardToken(),
            network = chef.blockchainGateway.network,
            amount = poolBlockRewards.toBigDecimal().divide(BigDecimal.TEN.pow(18), 18, RoundingMode.HALF_UP),
            tokenType = TokenType.SINGLE
        )
    }

    override suspend fun getRewardsPerSecond(): List<Reward> {
        return listOf(
            getNativeReward()
        )
    }

    override suspend fun getStakedTokens(): List<StakedAsset> {
        val lpAddress = chef.poolInfo(poolId).lpToken
        val token = erc20Resource.getTokenInformation(chef.blockchainGateway.network, lpAddress)
        val balance = erc20Resource.getBalance(
            chef.blockchainGateway.network,
            lpAddress,
            chef.address
        )
        return listOf(
            StakedAsset(
                address = lpAddress,
                network = chef.blockchainGateway.network,
                amount = balance.toBigDecimal().divide(BigDecimal.TEN.pow(18), 18, RoundingMode.HALF_UP),
                tokenType = token.type
            )
        )
    }
}