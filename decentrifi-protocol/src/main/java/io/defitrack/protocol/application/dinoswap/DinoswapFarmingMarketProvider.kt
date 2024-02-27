package io.defitrack.protocol.application.dinoswap

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
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
        }.mapNotNull {
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
            stakedToken = stakedtoken,
            rewardToken = rewardToken,
            positionFetcher = PositionFetcher(
                functionCreator = chef.dinoUserInfoFn(poolId)
            ),
            marketSize = refreshable {
                getMarketSize(stakedtoken, chef.address)
            },
            deprecated = true,
            type = "dinoswap.fossil-farms",
        )
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}