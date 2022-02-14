package io.defitrack.protocol.convex.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.CvxRewardPool
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.RewardToken
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
    objectMapper: ObjectMapper
) :
    UserStakingService(erC20Resource, objectMapper) {

    val cvxRewardPoolABI by lazy {
        abiResource.getABI("convex/CvxRewardPool.json")
    }

    override fun getStakings(address: String): List<StakingElement> {
        val cvxRewardPools = convexService.providePools().map {
            CvxRewardPool(
                ethereumContractAccessor,
                cvxRewardPoolABI,
                it.address,
                it.name
            )
        }

        return cvxRewardPools.mapNotNull { pool ->
            val balance = pool.balanceOf(address)
            if (balance > BigInteger.ZERO) {

                val rewardToken = erC20Resource.getTokenInformation(getNetwork(), pool.rewardToken())
                val stakedToken = erC20Resource.getTokenInformation(getNetwork(), pool.stakingToken())

                stakingElement(
                    user = address,
                    vaultUrl = "https://etherscan.io/address/${pool.address}",
                    vaultName = pool.name,
                    rewardTokens = listOf(
                        RewardToken(
                            name = rewardToken.name,
                            symbol = rewardToken.symbol,
                            decimals = rewardToken.decimals,
                        )
                    ),
                    stakedToken = stakedToken(
                        address = stakedToken.address,
                    ),
                    vaultType = "convex-reward-pool",
                    vaultAddress = pool.address,
                    amount = balance,
                    id = "cvx-rewardpool-${pool.address}"
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