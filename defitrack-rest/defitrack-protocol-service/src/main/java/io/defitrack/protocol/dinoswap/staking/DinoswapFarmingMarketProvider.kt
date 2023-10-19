package io.defitrack.protocol.dinoswap.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dinoswap.DinoswapService
import io.defitrack.protocol.dinoswap.contract.DinoswapFossilFarmsContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.DINOSWAP)
class DinoswapFarmingMarketProvider(
    private val dinoswapService: DinoswapService,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        return@coroutineScope dinoswapService.getDinoFossilFarms().map {
            DinoswapFossilFarmsContract(
                getBlockchainGateway(),
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength()).map { poolId ->
                async {
                    try {
                        toStakingMarketElement(chef, poolId)
                    } catch (e: Exception) {
                        logger.error("Error while fetching market", e)
                        null
                    }
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.DINOSWAP
    }

    private suspend fun toStakingMarketElement(
        chef: DinoswapFossilFarmsContract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken = getToken(chef.getLpTokenForPoolId(poolId))
        val rewardToken = getToken(chef.rewardToken())

        return create(
            identifier = "${chef.address}-${poolId}",
            name = stakedtoken.name + " Farm",
            stakedToken = stakedtoken.toFungibleToken(),
            rewardTokens = listOf(
                rewardToken.toFungibleToken()
            ),
            positionFetcher = PositionFetcher(
                address = chef.address,
                function = { user -> chef.userInfoFunction(user, poolId) }
            ),
            marketSize = Refreshable.refreshable {
                marketSizeService.getMarketSize(
                    stakedtoken.toFungibleToken(),
                    chef.address,
                    getNetwork()
                )
            },
        )
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}