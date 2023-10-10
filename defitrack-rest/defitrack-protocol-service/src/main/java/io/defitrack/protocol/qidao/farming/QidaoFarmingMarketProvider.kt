package io.defitrack.protocol.qidao.farming

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.qidao.QidaoPolygonService
import io.defitrack.protocol.qidao.contract.QidaoFarmV2Contract
import io.defitrack.token.DecentrifiERC20Resource
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.QIDAO)
class QidaoFarmingMarketProvider(
    private val qidaoPolygonService: QidaoPolygonService,
    private val tokenService: DecentrifiERC20Resource,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return qidaoPolygonService.farms().flatMap { farm ->
            val contract = QidaoFarmV2Contract(
                getBlockchainGateway(),
                farm
            )

            (0 until contract.poolLength()).map { poolId ->
                toStakingMarketElement(contract, poolId)
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.QIDAO
    }

    private suspend fun toStakingMarketElement(
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
            balanceFetcher = PositionFetcher(
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