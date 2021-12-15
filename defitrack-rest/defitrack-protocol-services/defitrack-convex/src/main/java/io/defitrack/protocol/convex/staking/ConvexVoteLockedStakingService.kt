package io.defitrack.protocol.convex.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.CvxLocker
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.staking.domain.VaultRewardToken
import io.defitrack.token.TokenService
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class ConvexVoteLockedStakingService(
    private val convexService: ConvexService,
    private val abiResource: ABIResource,
    private val ethereumContractAccessor: EthereumContractAccessor,
    tokenService: TokenService,
    objectMapper: ObjectMapper
) :
    UserStakingService(tokenService, objectMapper) {

    val cvxRewardPoolABI by lazy {
        abiResource.getABI("convex/CvxLocker.json")
    }

    override fun getStakings(address: String): List<StakingElement> {
        val cvxRewardPools = listOf(convexService.lockedRewardPool()).map {
            CvxLocker(
                ethereumContractAccessor,
                cvxRewardPoolABI,
                it.address,
                it.name
            )
        }

        return cvxRewardPools.mapNotNull { pool ->
            val balance = pool.balances(address)
            if (balance > BigInteger.ZERO) {

                val rewardToken = tokenService.getTokenInformation(pool.rewardToken(), getNetwork())
                val stakedToken = tokenService.getTokenInformation(pool.stakingToken(), getNetwork())

                StakingElement(
                    network = getNetwork(),
                    protocol = getProtocol(),
                    id = "convex-ethereum-${pool.address}",
                    user = address,
                    name = pool.name,
                    rewardTokens = listOf(
                        VaultRewardToken(
                            name = rewardToken.name,
                            symbol = rewardToken.symbol,
                            decimals = rewardToken.decimals,
                        )
                    ),
                    stakedToken = vaultStakedToken(
                        address = stakedToken.address,
                        amount = balance
                    ),
                    vaultType = "convex-locked-vote",
                    contractAddress = pool.address
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