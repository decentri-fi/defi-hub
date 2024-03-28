package io.defitrack.protocol.application.idex

import arrow.core.Either
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.idex.IdexFarmContract
import io.defitrack.protocol.idex.IdexService
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.IDEX)
class IdexFarmingMarketProvider(private val idexService: IdexService) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        idexService.idexFarm().map {
            IdexFarmContract(
                getBlockchainGateway(),
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength().toInt()).parMapNotNull(EmptyCoroutineContext, 12) { poolId ->
                Either.catch {
                    toStakingMarketElement(chef, poolId)
                }
            }
        }
    }.mapNotNull {
        it.mapLeft {
            logger.error("Error fetching market", it)
        }.getOrNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.IDEX
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    private suspend fun toStakingMarketElement(
        chef: IdexFarmContract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken = getToken(chef.getLpTokenForPoolId(poolId))
        val rewardToken = getToken(chef.rewardToken())
        return create(
            identifier = "${chef.address}-${poolId}",
            name = stakedtoken.name + " Farm",
            stakedToken = stakedtoken,
            rewardToken = rewardToken,
            positionFetcher = PositionFetcher(chef.userInfoFunction(poolId)),
            type = "idex.farm"
        )
    }
}