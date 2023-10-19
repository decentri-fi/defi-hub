package io.defitrack.protocol.dinoswap.staking

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dinoswap.DinoswapService
import io.defitrack.protocol.dinoswap.contract.DinoswapFossilFarmsContract
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

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
            (0 until chef.poolLength.await().toInt()).parMap(EmptyCoroutineContext, 12) { poolId ->
                catch {
                    toStakingMarketElement(chef, poolId)
                }
            }
        }.mapNotNull{
            it.mapLeft {
                logger.error("Failed to fetch Dinoswap market", it)
            }.getOrNull()
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.DINOSWAP
    }

    private suspend fun toStakingMarketElement(
        chef: DinoswapFossilFarmsContract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken = getToken(chef.lpTokenForPoolId(poolId))
        val rewardToken = getToken(chef.rewardToken.await())

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
            marketSize = refreshable {
                marketSizeService.getMarketSize(
                    stakedtoken.toFungibleToken(),
                    chef.address,
                    getNetwork()
                )
            },
            rewardsFinished = true
        )
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}