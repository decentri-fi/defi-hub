package io.defitrack.protocol.dinoswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.polygon.config.PolygonContractAccessorConfig
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dinoswap.DinoswapFossilFarmsContract
import io.defitrack.protocol.dinoswap.DinoswapService
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DinoswapStakingMarketService(
    private val dinoswapService: DinoswapService,
    private val abiResource: ABIResource,
    private val tokenService: ERC20Resource,
    private val contractAccessorGateway: ContractAccessorGateway
) : StakingMarketService() {

    val fossilFarms by lazy {
        abiResource.getABI("dinoswap/FossilFarms.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarketElement> {
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

    private fun toStakingMarketElement(
        chef: DinoswapFossilFarmsContract,
        poolId: Int
    ): StakingMarketElement {
        val stakedtoken =
            tokenService.getTokenInformation(getNetwork(), chef.getLpTokenForPoolId(poolId))
        val rewardToken = tokenService.getTokenInformation(getNetwork(), chef.rewardToken)
        return StakingMarketElement(
            id = "dinoswap-${chef.address}-${poolId}",
            network = getNetwork(),
            name = stakedtoken.name + " Farm",
            protocol = getProtocol(),
            token = stakedtoken.toFungibleToken(),
            reward = listOf(
                rewardToken.toFungibleToken()
            ),
            contractAddress = chef.address,
            vaultType = "dinoswap-fossilfarm"
        )
    }


    override fun getProtocol(): Protocol {
        return Protocol.DINOSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}