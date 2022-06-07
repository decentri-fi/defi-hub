package io.defitrack.protocol.dinoswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dinoswap.DinoswapFossilFarmsContract
import io.defitrack.protocol.dinoswap.DinoswapService
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.staking.domain.StakingMarket
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DinoswapStakingMarketService(
    private val dinoswapService: DinoswapService,
    private val abiResource: ABIResource,
    private val tokenService: ERC20Resource,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
    private val contractAccessorGateway: ContractAccessorGateway
) : StakingMarketService() {

    val fossilFarms by lazy {
        abiResource.getABI("dinoswap/FossilFarms.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarket> {
        return dinoswapService.getDinoFossilFarms().map {
            DinoswapFossilFarmsContract(
                contractAccessorGateway.getGateway(getNetwork()),
                fossilFarms,
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength).map { poolId ->
                toStakingMarketElement(chef, poolId)
            }
        }
    }

    private suspend fun toStakingMarketElement(
        chef: DinoswapFossilFarmsContract,
        poolId: Int
    ): StakingMarket {
        val stakedtoken =
            tokenService.getTokenInformation(getNetwork(), chef.getLpTokenForPoolId(poolId))
        val rewardToken = tokenService.getTokenInformation(getNetwork(), chef.rewardToken)

        val marketBalance = erC20Resource.getBalance(getNetwork(), stakedtoken.address, chef.address)
        val marketSize = priceResource.calculatePrice(PriceRequest(
            stakedtoken.address,
            getNetwork(),
            marketBalance.asEth(stakedtoken.decimals),
            stakedtoken.type
        ))

        return StakingMarket(
            id = "dinoswap-${chef.address}-${poolId}",
            network = getNetwork(),
            name = stakedtoken.name + " Farm",
            protocol = getProtocol(),
            stakedToken = stakedtoken.toFungibleToken(),
            rewardTokens = listOf(
                rewardToken.toFungibleToken()
            ),
            contractAddress = chef.address,
            vaultType = "dinoswap-fossilfarm",
            balanceFetcher = StakingMarketBalanceFetcher(
                address = chef.address,
                function = { user -> chef.userInfoFunction(user, poolId)}
            ),
            marketSize = marketSize.toBigDecimal()
        )
    }


    override fun getProtocol(): Protocol {
        return Protocol.DINOSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}