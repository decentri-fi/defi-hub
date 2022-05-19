package io.defitrack.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.StargateOptimismService
import io.defitrack.protocol.contract.LPStakingContract
import io.defitrack.staking.domain.StakingMarket
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class StargateStakingMarketService(
    private val stargateOptimismService: StargateOptimismService,
    private val accessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource
) : StakingMarketService() {

    val lpStakingContractAbi by lazy {
        abiResource.getABI("stargate/LPStaking.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarket> {
        val lpStakingContract = LPStakingContract(
            accessorGateway.getGateway(getNetwork()),
            lpStakingContractAbi,
            stargateOptimismService.getLpFarm()
        )
        val stargate = erC20Resource.getTokenInformation(getNetwork(), lpStakingContract.stargate)

        return lpStakingContract.poolInfos.mapIndexed { index, info ->
            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), info.lpToken)
            val rewardTokens = listOf(stargate)

            stakingMarket(
                id = "$lpStakingContract-$index",
                name = "Stargate ${stakedToken.name} Reward",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = rewardTokens.map { it.toFungibleToken() },
                contractAddress = lpStakingContract.address,
                vaultType = "stargate-lp-staking"
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.STARGATE
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}