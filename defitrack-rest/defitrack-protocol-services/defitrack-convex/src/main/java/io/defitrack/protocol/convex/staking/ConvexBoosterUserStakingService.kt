package io.defitrack.protocol.convex.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessorConfig
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.contract.ConvexBoosterContract
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class ConvexBoosterUserStakingService(
    private val convexService: ConvexService,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource,
) : UserStakingService(
    erC20Resource
) {


    override fun getStakings(address: String): List<StakingElement> {
        val gateway = contractAccessorGateway.getGateway(getNetwork())

        val booster = ConvexBoosterContract(
            gateway,
            abiResource.getABI("convex/Booster.json"),
            convexService.provideBooster()
        )

        val poolInfos = booster.poolInfos

        return erC20Resource.getBalancesFor(address, poolInfos.map { it.crvRewards }, gateway)
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val stakedToken = erC20Resource.getTokenInformation(getNetwork(), poolInfos[index].lpToken)

                    StakingElement(
                        id = "convex-booster-$index",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        name = "Convex Crv Booster $index",
                        contractAddress = booster.address,
                        vaultType = "convex-crv-rewards",
                        stakedToken = stakedToken.toFungibleToken(),
                        amount = balance,
                        rewardTokens = emptyList()
                    )
                } else {
                    null
                }
            }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}