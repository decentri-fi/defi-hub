package io.defitrack.protocol.convex.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.contract.CvxRewardPoolContract
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class ConvexPoolsUserStakingService(
    private val convexService: ConvexService,
    private val abiResource: ABIResource,
    private val ethereumContractAccessor: EthereumContractAccessor,
    erC20Resource: ERC20Resource,
) :
    UserStakingService(erC20Resource) {

    val cvxRewardPoolABI by lazy {
        abiResource.getABI("convex/CvxRewardPool.json")
    }

    override fun getStakings(address: String): List<StakingElement> {
        val cvxRewardPools = convexService.providePools().map {
            CvxRewardPoolContract(
                ethereumContractAccessor,
                cvxRewardPoolABI,
                it.address,
                it.name
            )
        }

        return erC20Resource.getBalancesFor(address, cvxRewardPools.map { it.address }, ethereumContractAccessor)
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val pool = cvxRewardPools[index]
                    val rewardToken = erC20Resource.getTokenInformation(getNetwork(), pool.rewardToken())
                    val stakedToken = erC20Resource.getTokenInformation(getNetwork(), pool.stakingToken())

                    stakingElement(
                        vaultName = pool.name,
                        rewardTokens = listOf(
                            rewardToken.toFungibleToken()
                        ),
                        stakedToken = stakedToken.toFungibleToken(),
                        vaultType = "convex-reward-pool",
                        vaultAddress = pool.address,
                        amount = balance,
                        id = "cvx-rewardpool-${pool.address}"
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