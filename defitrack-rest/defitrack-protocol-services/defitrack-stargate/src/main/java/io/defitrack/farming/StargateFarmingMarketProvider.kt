package io.defitrack.farming

import io.defitrack.abi.ABIResource
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.StargateService
import io.defitrack.protocol.contract.LPStakingContract
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
abstract class StargateFarmingMarketProvider(
    private val stargateOptimismService: StargateService,
    private val accessorGateway: BlockchainGatewayProvider,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource
) : FarmingMarketProvider() {

    val lpStakingContractAbi by lazy {
        abiResource.getABI("stargate/LPStaking.json")
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val lpStakingContract = LPStakingContract(
            accessorGateway.getGateway(getNetwork()),
            lpStakingContractAbi,
            stargateOptimismService.getLpFarm()
        )
        val stargate = erC20Resource.getTokenInformation(getNetwork(), lpStakingContract.stargate())

        return lpStakingContract.poolInfos().mapIndexed { index, info ->
            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), info.lpToken)
            val rewardTokens = listOf(stargate)

            create(
                identifier = "$lpStakingContract-$index",
                name = "Stargate ${stakedToken.name} Reward",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = rewardTokens.map { it.toFungibleToken() },
                vaultType = "stargate-lp-staking"
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.STARGATE
    }
}