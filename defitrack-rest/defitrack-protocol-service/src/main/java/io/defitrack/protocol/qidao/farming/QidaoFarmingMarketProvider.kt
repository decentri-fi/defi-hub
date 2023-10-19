package io.defitrack.protocol.qidao.farming

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.qidao.QidaoPolygonService
import io.defitrack.protocol.qidao.contract.QidaoFarmV2Contract
import io.defitrack.token.DecentrifiERC20Resource
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.QIDAO)
class QidaoFarmingMarketProvider(
    private val qidaoPolygonService: QidaoPolygonService,
    private val tokenService: DecentrifiERC20Resource,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return qidaoPolygonService.farms().parMap{ farm ->
            importPoolsFromFarm(farm)
        }.flatten()
    }

    private suspend fun importPoolsFromFarm(farm: String): List<FarmingMarket> {
        val contract = QidaoFarmV2Contract(
            getBlockchainGateway(),
            farm
        )

        return (0 until contract.poolLength()).parMap(EmptyCoroutineContext, 8) { poolId ->
            toMarket(contract, poolId)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.QIDAO
    }

    private suspend fun toMarket(
        chef: QidaoFarmV2Contract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken =
            tokenService.getTokenInformation(getNetwork(), chef.getLpTokenForPoolId(poolId))
        val rewardToken = tokenService.getTokenInformation(getNetwork(), chef.rewardToken())


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
        )
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}