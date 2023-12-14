package io.defitrack.farming

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.contract.MasterChefBasedContract
import io.defitrack.protocol.swapfish.SwapfishArbitrumService
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SWAPFISH)
class SwapfishArbitrumMasterchefFarmingMarketProvider(
    private val swapfishArbitrumService: SwapfishArbitrumService
) : FarmingMarketProvider() {
    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        swapfishArbitrumService.provideMasterchefs().flatMap { masterChefAddr ->
            val masterchef = MasterChefBasedContract(
                rewardTokenName = "cake",
                pendingName = "pendingCake",
                getBlockchainGateway(),
                masterChefAddr
            )
            (0 until masterchef.poolLength.await().toInt()).parMap(concurrency = 12) { poolId ->
                toStakingMarketElement(masterchef, poolId)
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SWAPFISH
    }

    private suspend fun toStakingMarketElement(
        chef: MasterChefBasedContract,
        poolId: Int
    ): FarmingMarket? {
        return try {
            val stakedtoken = getToken(chef.getLpTokenForPoolId(poolId).lpToken)
            val rewardToken = getToken(chef.rewardToken.await())
            create(
                identifier = "${chef.address}-${poolId}",
                name = stakedtoken.name + " Farm",
                stakedToken = stakedtoken,
                rewardTokens = listOf(
                    rewardToken
                ),
                marketSize = refreshable {
                    getMarketSize(stakedtoken, chef.address)
                },
                positionFetcher = PositionFetcher(
                    chef.userInfoFunction(poolId)
                ),
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
