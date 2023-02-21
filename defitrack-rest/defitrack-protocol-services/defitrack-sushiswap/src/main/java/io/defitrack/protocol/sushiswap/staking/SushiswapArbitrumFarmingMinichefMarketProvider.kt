package io.defitrack.protocol.sushiswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceResource
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiArbitrumService
import io.defitrack.protocol.reward.MiniChefV2Contract
import io.defitrack.protocol.sushiswap.apr.MinichefStakingAprCalculator
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class SushiswapArbitrumFarmingMinichefMarketProvider(
    private val abiResource: ABIResource,
    private val priceResource: PriceResource,
) : FarmingMarketProvider() {

    val minichefABI by lazy {
        abiResource.getABI("sushi/MiniChefV2.json")
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        SushiArbitrumService.getMiniChefs().map {
            MiniChefV2Contract(
                getBlockchainGateway(),
                minichefABI,
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

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }

    private suspend fun toStakingMarketElement(
        chef: MiniChefV2Contract,
        poolId: Int
    ): FarmingMarket? {
        return try {
            val stakedtoken = getToken(chef.getLpTokenForPoolId(poolId))
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
                apr = MinichefStakingAprCalculator(getERC20Resource(), priceResource, chef, poolId).calculateApr(),
                balanceFetcher = PositionFetcher(
                    chef.address,
                    { user -> chef.userInfoFunction(poolId, user) }
                ),
                farmType = FarmType.LIQUIDITY_MINING
            )
        } catch (ex: Exception) {
            logger.error("Error while fetching market for poolId $poolId", ex)
            null
        }
    }
}