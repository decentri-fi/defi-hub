package io.defitrack.protocol.sushiswap.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.reward.MiniChefV2Contract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

abstract class SushiMinichefV2FarmingMarketProvider(
    val addresses: List<String>
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        addresses.map {
            MiniChefV2Contract(
                getBlockchainGateway(),
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength()).map { poolId ->
                async {
                    toStakingMarketElement(chef, poolId)
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    private suspend fun toStakingMarketElement(
        chef: MiniChefV2Contract,
        poolId: Int
    ): FarmingMarket? {
        return try {
            val stakedtoken = getToken(chef.getLpTokenForPoolId(poolId))
            val rewardToken = getToken(chef.rewardToken.await())
            create(
                identifier = "${chef.address}-${poolId}",
                name = stakedtoken.name + " Farm",
                stakedToken = stakedtoken,
                rewardTokens = listOf(
                    rewardToken.toFungibleToken()
                ),
                marketSize = Refreshable.refreshable {
                    getMarketSize(stakedtoken, chef.address)
                },
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken,
                        contractAddress = chef.address,
                        getRewardFunction = chef.pendingSushiFunction(poolId)
                    ),
                    preparedTransaction = selfExecutingTransaction(chef.harvestFunction(poolId))
                ),
                positionFetcher = PositionFetcher(
                    chef.address,
                    chef.userInfoFunction(poolId)
                ),
            )
        } catch (ex: Exception) {
            logger.error("Error while fetching market for poolId $poolId", ex)
            null
        }
    }
}