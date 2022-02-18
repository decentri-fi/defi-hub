package io.defitrack.protocol.convex.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.contract.ConvexBoosterContract
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakedToken
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class ConvexBoosterUserStakingService(
    private val convexService: ConvexService,
    private val ethereumContractAccessor: EthereumContractAccessor,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource,
) : UserStakingService(
    erC20Resource
) {


    override fun getStakings(address: String): List<StakingElement> {
        val booster = ConvexBoosterContract(
            ethereumContractAccessor,
            abiResource.getABI("convex/Booster.json"),
            convexService.provideBooster()
        )

        val poolInfos = booster.poolInfos


        return ethereumContractAccessor.readMultiCall(poolInfos.map {
            MultiCallElement(
                ERC20Contract(
                    ethereumContractAccessor,
                    abiResource.getABI("convex/ERC20.json"),
                    it.crvRewards
                ).balanceOfMethod(address),
                it.crvRewards
            )
        }).mapIndexed { index, retVal ->
            val balance = retVal[0].value as BigInteger
            if (balance > BigInteger.ZERO) {
                val stakedToken = erC20Resource.getTokenInformation(getNetwork(), poolInfos[index].lpToken)

                StakingElement(
                    id = "convex-booster-$index",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = "Convex Crv Booster $index",
                    contractAddress = booster.address,
                    vaultType = "convex-crv-rewards",
                    stakedToken = StakedToken(
                        stakedToken.name,
                        stakedToken.symbol,
                        stakedToken.address,
                        getNetwork(),
                        stakedToken.decimals,
                        stakedToken.type
                    ),
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