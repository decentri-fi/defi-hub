package io.defitrack.protocol.idex

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class IdexFarmingMarketService(
    private val abiResource: ABIResource,
    private val tokenService: ERC20Resource,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val idexService: IdexService
) : StakingMarketService() {


    val minichefABI by lazy {
        abiResource.getABI("idex/IdexFarm.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarketElement> {
        return idexService.idexFarm().map {
            IdexFarmContract(
                contractAccessorGateway.getGateway(getNetwork()),
                minichefABI,
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength).mapNotNull { poolId ->
                try {
                    val farm = toStakingMarketElement(chef, poolId)
                    farm
                } catch (ex: Exception) {
                    logger.debug("something went wrong trying to import idex pool", ex)
                    null
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.IDEX
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    private fun toStakingMarketElement(
        chef: IdexFarmContract,
        poolId: Int
    ): StakingMarketElement {
        val stakedtoken =
            tokenService.getTokenInformation(getNetwork(), chef.getLpTokenForPoolId(poolId))
        val rewardToken = tokenService.getTokenInformation(getNetwork(), chef.rewardToken)
        return StakingMarketElement(
            id = "idex-${chef.address}-${poolId}",
            network = getNetwork(),
            name = stakedtoken.name + " Farm",
            protocol = getProtocol(),
            stakedToken = stakedtoken.toFungibleToken(),
            rewardTokens = listOf(
                rewardToken.toFungibleToken()
            ),
            contractAddress = chef.address,
            vaultType = "idex-farm",
            balanceFetcher = StakingMarketBalanceFetcher(
                chef.address,
                { user -> chef.userInfoFunction(poolId, user) }
            )
        )
    }
}