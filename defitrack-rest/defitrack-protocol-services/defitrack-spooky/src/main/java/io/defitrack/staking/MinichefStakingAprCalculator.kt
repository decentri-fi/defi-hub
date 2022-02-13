package io.defitrack.staking

import io.defitrack.apr.Reward
import io.defitrack.apr.StakedAsset
import io.defitrack.apr.StakingAprCalculator
import io.defitrack.price.PriceResource
import io.defitrack.protocol.reward.MasterchefLpContract
import io.defitrack.token.Token
import io.defitrack.token.TokenType
import io.defitrack.token.ERC20Resource
import java.math.BigDecimal
import java.math.RoundingMode

class MinichefStakingAprCalculator(
    private val erC20Resource: ERC20Resource,
    priceResource: PriceResource,
    private val chef: MasterchefLpContract,
    private val poolId: Int,
    private val stakedToken: Token
) : StakingAprCalculator(priceResource) {

    fun getNativeReward(): Reward {
        val poolInfo = chef.poolInfo(poolId);
        val allSushiPerSecond = chef.sushiPerSecond
        val poolBlockRewards = allSushiPerSecond.times(poolInfo.allocPoint).divide(chef.totalAllocPoint)
        return Reward(
            address = chef.rewardToken,
            network = chef.evmContractAccessor.getNetwork(),
            amount = poolBlockRewards.toBigDecimal().divide(BigDecimal.TEN.pow(18), 18, RoundingMode.HALF_UP),
            tokenType = TokenType.SINGLE
        )
    }

    override fun getRewardsPerSecond(): List<Reward> {
        return listOf(
            getNativeReward()
        )
    }

    override fun getStakedTokens(): List<StakedAsset> {
        val balance = erC20Resource.getBalance(
            chef.evmContractAccessor.getNetwork(),
            stakedToken.address,
            chef.address
        )
        return listOf(
            StakedAsset(
                address = stakedToken.address,
                network = chef.evmContractAccessor.getNetwork(),
                amount = balance.toBigDecimal().divide(BigDecimal.TEN.pow(18), 18, RoundingMode.HALF_UP),
                tokenType = stakedToken.type
            )
        )
    }
}