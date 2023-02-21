package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.QidaoPolygonService
import io.defitrack.protocol.contract.QidaoFarmV2Contract
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class QidaoFarmingMarketProvider(
    private val qidaoPolygonService: QidaoPolygonService,
    private val tokenService: ERC20Resource,
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
            vaultType = "qidao-farmv2",
            balanceFetcher = PositionFetcher(
                address = chef.address,
                function = { user -> chef.userInfoFunction(user, poolId) }
            ),
            marketSize = marketSizeService.getMarketSize(
                stakedtoken.toFungibleToken(),
                chef.address,
                getNetwork()
            ),
            farmType = ContractType.LIQUIDITY_MINING
        )
    }


    override fun getProtocol(): Protocol {
        return Protocol.QIDAO
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}