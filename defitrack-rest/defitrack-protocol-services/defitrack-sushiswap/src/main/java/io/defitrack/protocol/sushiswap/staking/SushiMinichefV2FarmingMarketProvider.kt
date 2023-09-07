package io.defitrack.protocol.sushiswap.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.reward.MiniChefV2Contract
import io.defitrack.protocol.sushiswap.apr.MinichefStakingAprCalculator
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

abstract class SushiMinichefV2FarmingMarketProvider(
    val addresses: List<String>
): FarmingMarketProvider() {

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
                stakedToken = stakedtoken.toFungibleToken(),
                rewardTokens = listOf(
                    rewardToken.toFungibleToken()
                ),
                vaultType = "sushi-minichefV2",
                marketSize = Refreshable.refreshable {
                    getMarketSize(stakedtoken.toFungibleToken(), chef.address)
                },
                claimableRewardFetcher = ClaimableRewardFetcher(
                    address = chef.address,
                    function = { user ->
                        chef.pendingSushiFunction(poolId, user)
                    },
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            network = getNetwork().toVO(),
                            chef.harvestFunction(poolId, user),
                            to = chef.address,
                            from = user
                        )
                    }
                ),
                apr = MinichefStakingAprCalculator(getERC20Resource(), getPriceResource(), chef, poolId).calculateApr(),
                balanceFetcher = PositionFetcher(
                    chef.address,
                    { user -> chef.userInfoFunction(poolId, user) }
                ),
                farmType = ContractType.LIQUIDITY_MINING
            )
        } catch (ex: Exception) {
            logger.error("Error while fetching market for poolId $poolId", ex)
            null
        }
    }
}