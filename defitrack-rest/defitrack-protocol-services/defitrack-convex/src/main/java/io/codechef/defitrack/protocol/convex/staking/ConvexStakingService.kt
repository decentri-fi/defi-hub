package io.codechef.defitrack.protocol.convex.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.codechef.defitrack.staking.UserStakingService
import io.codechef.defitrack.staking.domain.StakingElement
import io.codechef.defitrack.staking.domain.VaultRewardToken
import io.codechef.defitrack.token.TokenService
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.CvxRewardPool
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class ConvexStakingService(
    private val convexService: ConvexService,
    private val abiResource: ABIResource,
    private val ethereumContractAccessor: EthereumContractAccessor,
    tokenService: TokenService,
    objectMapper: ObjectMapper
) :
    UserStakingService(tokenService, objectMapper) {

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

                val rewardToken = tokenService.getTokenInformation(pool.rewardToken(), getNetwork())
                val stakedToken = tokenService.getTokenInformation(pool.stakingToken(), getNetwork())

                stakingElement(
                    user = address,
                    vaultUrl = "https://etherscan.io/address/${pool.address}",
                    vaultName = pool.name,
                    rewardTokens = listOf(
                        VaultRewardToken(
                            name = rewardToken.name,
                            symbol = rewardToken.symbol,
                            decimals = rewardToken.decimals,
                            url = "https://etherscan.io/address/${rewardToken.address}"
                        )
                    ),
                    stakedToken = vaultStakedToken(
                        address = stakedToken.address,
                        amount = balance
                    ),
                    vaultType = "convext-reward-pool",
                    vaultAddress = pool.address
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