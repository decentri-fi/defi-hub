package io.defitrack.protocol.convex.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.contract.ConvexBoosterContract
import io.defitrack.staking.StakingPositionService
import io.defitrack.staking.domain.StakingPosition
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class ConvexBoosterStakingPositionService(
    private val convexService: ConvexService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource,
) : StakingPositionService(
    erC20Resource
) {


    override suspend fun getStakings(address: String): List<StakingPosition> {
        val gateway = blockchainGatewayProvider.getGateway(getNetwork())

        val booster = ConvexBoosterContract(
            gateway,
            abiResource.getABI("convex/Booster.json"),
            convexService.provideBooster()
        )

        val poolInfos = booster.poolInfos

        return erC20Resource.getBalancesFor(address, poolInfos.map { it.crvRewards }, getNetwork())
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val stakedToken = erC20Resource.getTokenInformation(getNetwork(), poolInfos[index].lpToken)

                    stakingElement(
                        id = "convex-booster-$index",
                        vaultName = "Convex Crv Booster $index",
                        vaultAddress = booster.address,
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