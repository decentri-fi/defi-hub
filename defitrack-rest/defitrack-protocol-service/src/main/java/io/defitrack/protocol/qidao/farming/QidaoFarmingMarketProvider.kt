package io.defitrack.protocol.qidao.farming

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.port.input.ERC20Resource
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.qidao.QidaoPolygonService
import io.defitrack.protocol.qidao.contract.QidaoFarmV2Contract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.QIDAO)
class QidaoFarmingMarketProvider(
    private val qidaoPolygonService: QidaoPolygonService,
    private val erc20Resource: ERC20Resource,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return qidaoPolygonService.farms().parMap(concurrency = 12) { farm ->
            importPoolsFromFarm(farm)
        }.flatten()
    }

    private suspend fun importPoolsFromFarm(farm: String): List<FarmingMarket> {
        val contract = QidaoFarmV2Contract(
            getBlockchainGateway(),
            farm
        )

        return (0 until contract.poolLength()).parMapNotNull(concurrency = 8) { poolId ->
            catch {
                toMarket(contract, poolId)
            }.mapLeft {
                logger.error("Error while fetching qidao market", it)
                null
            }.getOrNull()
        }
    }

    override fun getProtocol(): Protocol = Protocol.QIDAO

    private suspend fun toMarket(
        chef: QidaoFarmV2Contract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken = erc20Resource.getTokenInformation(getNetwork(), chef.getLpTokenForPoolId(poolId))
        val rewardToken = erc20Resource.getTokenInformation(getNetwork(), chef.rewardToken())

        return create(
            identifier = "${chef.address}-${poolId}",
            name = stakedtoken.name + " Farm",
            stakedToken = stakedtoken,
            rewardToken = rewardToken,
            positionFetcher = PositionFetcher(
                functionCreator = chef.userInfoFunction(poolId)
            ),
            marketSize = refreshable {
                marketSizeService.getMarketSize(
                    stakedtoken,
                    chef.address,
                    getNetwork()
                ).usdAmount
            },
        )
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}