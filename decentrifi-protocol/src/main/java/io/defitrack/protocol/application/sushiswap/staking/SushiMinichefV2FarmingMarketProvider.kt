package io.defitrack.protocol.application.sushiswap.staking

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.reward.MiniChefV2Contract
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
                rewardToken = rewardToken,
                marketSize = refreshable {
                    getMarketSize(stakedtoken, chef.address)
                },
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken,
                        getRewardFunction = chef.pendingSushiFunction(poolId)
                    ),
                    preparedTransaction = selfExecutingTransaction(chef.harvestFunction(poolId))
                ),
                positionFetcher = PositionFetcher(
                    chef.userInfoFunction(poolId)
                ),
                type = "sushiswap.minichef.v2"
            )
        } catch (ex: Exception) {
            logger.error("Error while fetching market for poolId $poolId", ex)
            null
        }
    }
}