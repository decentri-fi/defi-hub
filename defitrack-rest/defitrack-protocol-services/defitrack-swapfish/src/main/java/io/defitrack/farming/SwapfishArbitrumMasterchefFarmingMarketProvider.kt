package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.SwapfishArbitrumService
import io.defitrack.protocol.contract.MasterChefBasedContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class SwapfishArbitrumMasterchefFarmingMarketProvider(
    private val swapfishArbitrumService: SwapfishArbitrumService
) : FarmingMarketProvider() {
    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        swapfishArbitrumService.provideMasterchefs().flatMap { masterChefAddr ->
            val masterchef = MasterChefBasedContract(
                rewardTokenName = "cake",
                perSecondName = "cakePerSecond",
                pendingName = "pendingCake",
                getBlockchainGateway(),
                masterChefAddr
            )
            (0 until masterchef.poolLength()).map { poolId ->
                async {
                    toStakingMarketElement(masterchef, poolId)
                }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun toStakingMarketElement(
        chef: MasterChefBasedContract,
        poolId: Int
    ): FarmingMarket? {
        return try {
            val stakedtoken = getToken(chef.getLpTokenForPoolId(poolId).lpToken)
            val rewardToken = getToken(chef.rewardToken())
            create(
                identifier = "${chef.address}-${poolId}",
                name = stakedtoken.name + " Farm",
                stakedToken = stakedtoken.toFungibleToken(),
                rewardTokens = listOf(
                    rewardToken.toFungibleToken()
                ),
                vaultType = "sushi-minichefV2",
                marketSize = getMarketSize(stakedtoken.toFungibleToken(), chef.address),
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

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}
