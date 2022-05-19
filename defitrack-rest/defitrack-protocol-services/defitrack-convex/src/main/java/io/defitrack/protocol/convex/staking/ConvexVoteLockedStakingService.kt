package io.defitrack.protocol.convex.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.contract.CvxLockerContract
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingPosition
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class ConvexVoteLockedStakingService(
    private val convexService: ConvexService,
    private val abiResource: ABIResource,
    private val contractAccessorGateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource
) :
    UserStakingService(erC20Resource) {

    val cvxRewardPoolABI by lazy {
        abiResource.getABI("convex/CvxLocker.json")
    }

    override fun getStakings(address: String): List<StakingPosition> {
        val cvxRewardPools = listOf(convexService.lockedRewardPool()).map {
            CvxLockerContract(
                contractAccessorGateway.getGateway(getNetwork()),
                cvxRewardPoolABI,
                it.address,
                it.name
            )
        }

        return cvxRewardPools.mapNotNull { pool ->
            val balance = pool.balances(address)
            if (balance > BigInteger.ZERO) {

                val rewardToken = erC20Resource.getTokenInformation(getNetwork(), pool.rewardToken())
                val stakedToken = erC20Resource.getTokenInformation(getNetwork(), pool.stakingToken())

                stakingElement(
                    id = "convex-ethereum-${pool.address}",
                    vaultName = pool.name,
                    rewardTokens = listOf(
                        rewardToken.toFungibleToken()
                    ),
                    stakedToken = stakedToken.toFungibleToken(),
                    vaultType = "convex-locked-vote",
                    vaultAddress = pool.address,
                    amount = balance
                )
            } else {
                null
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}