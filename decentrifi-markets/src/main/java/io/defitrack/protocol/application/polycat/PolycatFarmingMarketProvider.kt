package io.defitrack.protocol.application.polycat

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.polycat.PolycatService
import io.defitrack.protocol.polycat.contract.PolycatMasterChefContract
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.POLYCAT)
class PolycatFarmingMarketProvider(
    private val polycatService: PolycatService,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return polycatService.getPolycatFarms().map {
            PolycatMasterChefContract(
                getBlockchainGateway(),
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength.await().toInt()).parMapNotNull(EmptyCoroutineContext, 12) { poolId ->
                catch {
                    toStakingMarketElement(chef, poolId)
                }.mapLeft { error ->
                    logger.error("Error while fetching polycat market", error)
                    null
                }.getOrNull()
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.POLYCAT
    }

    private suspend fun toStakingMarketElement(
        chef: PolycatMasterChefContract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken = getToken(chef.poolInfo(poolId).lpToken)
        val rewardToken = getToken(chef.rewardToken.await())
        return create(
            identifier = "${chef.address}-${poolId}",
            name = stakedtoken.name + " Farm",
            stakedToken = stakedtoken,
            rewardTokens = listOf(
                rewardToken
            ),
            type = "polycat.farm",
            positionFetcher = PositionFetcher(
                { user ->
                    chef.userInfoFunction(
                        user, poolId
                    )
                },
            ),
        )
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}