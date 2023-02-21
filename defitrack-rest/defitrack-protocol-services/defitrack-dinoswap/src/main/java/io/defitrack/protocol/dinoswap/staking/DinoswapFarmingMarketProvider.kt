package io.defitrack.protocol.dinoswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dinoswap.DinoswapService
import io.defitrack.protocol.dinoswap.contract.DinoswapFossilFarmsContract
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DinoswapFarmingMarketProvider(
    private val dinoswapService: DinoswapService,
    private val abiResource: ABIResource,
    private val tokenService: ERC20Resource,
    private val priceResource: PriceResource,
) : FarmingMarketProvider() {

    val fossilFarms by lazy {
        abiResource.getABI("dinoswap/FossilFarms.json")
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return dinoswapService.getDinoFossilFarms().map {
            DinoswapFossilFarmsContract(
                getBlockchainGateway(),
                fossilFarms,
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength()).map { poolId ->
                toStakingMarketElement(chef, poolId)
            }
        }
    }

    private suspend fun toStakingMarketElement(
        chef: DinoswapFossilFarmsContract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken =
            tokenService.getTokenInformation(getNetwork(), chef.getLpTokenForPoolId(poolId))
        val rewardToken = tokenService.getTokenInformation(getNetwork(), chef.rewardToken())

        val marketBalance = erC20Resource.getBalance(getNetwork(), stakedtoken.address, chef.address)
        val marketSize = priceResource.calculatePrice(
            PriceRequest(
                stakedtoken.address,
                getNetwork(),
                marketBalance.asEth(stakedtoken.decimals),
                stakedtoken.type
            )
        )

        return create(
            identifier = "${chef.address}-${poolId}",
            name = stakedtoken.name + " Farm",
            stakedToken = stakedtoken.toFungibleToken(),
            rewardTokens = listOf(
                rewardToken.toFungibleToken()
            ),
            vaultType = "dinoswap-fossilfarm",
            balanceFetcher = PositionFetcher(
                address = chef.address,
                function = { user -> chef.userInfoFunction(user, poolId) }
            ),
            marketSize = marketSize.toBigDecimal(),
            farmType = FarmType.LIQUIDITY_MINING
        )
    }


    override fun getProtocol(): Protocol {
        return Protocol.DINOSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}