package io.defitrack.protocol.sushiswap.apr

import io.defitrack.apr.Reward
import io.defitrack.apr.StakedAsset
import io.defitrack.apr.StakingAprCalculator
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.price.PriceResource
import io.defitrack.protocol.contract.MasterChefBasedContract
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import java.math.BigDecimal
import java.math.RoundingMode

class MasterchefBasedfStakingAprCalculator(
    private val erC20Resource: ERC20Resource,
    priceResource: PriceResource,
    private val chef: MasterChefBasedContract,
    private val poolId: Int
) : StakingAprCalculator(priceResource) {

    suspend fun getNativeReward(): Reward {
        val poolInfo = chef.poolInfos()[poolId]
        val allSushiPerSecond = chef.perSecond()
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
        val poolInfo = chef.getLpTokenForPoolId(poolId)
        val balance = erC20Resource.getBalance(
            chef.blockchainGateway.network,
            poolInfo.lpToken,
            chef.address
        )
        return listOf(
            StakedAsset(
                address = poolInfo.lpToken,
                network = chef.blockchainGateway.network,
                amount = balance.toBigDecimal().dividePrecisely(BigDecimal.TEN.pow(18)),
                tokenType = TokenType.SUSHISWAP
            )
        )
    }
}