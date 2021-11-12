package io.codechef.defitrack.protocol.quickswap.claimable

import io.codechef.common.network.Network
import io.codechef.defitrack.abi.ABIResource
import io.codechef.defitrack.claimable.ClaimableElement
import io.codechef.defitrack.claimable.ClaimableService
import io.codechef.defitrack.claimable.ClaimableToken
import io.codechef.defitrack.token.TokenService
import io.codechef.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toAddress
import io.codechef.ethereumbased.contract.multicall.MultiCallElement
import io.codechef.matic.config.PolygonContractAccessor
import io.codechef.protocol.Protocol
import io.codechef.quickswap.QuickswapRewardPoolContract
import io.codechef.quickswap.QuickswapService
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

@Service
class QuickswapClaimableService(
    private val quickswapService: QuickswapService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiService: ABIResource,
    private val tokenService: TokenService
) : ClaimableService {

    val stakingRewardsABI by lazy {
        abiService.getABI("quickswap/StakingRewards.json")
    }

    override fun claimables(address: String): List<ClaimableElement> {
        val pools = quickswapService.getVaultAddresses().map {
            QuickswapRewardPoolContract(
                polygonContractAccessor,
                stakingRewardsABI,
                it,
            )
        }

        return polygonContractAccessor.readMultiCall(pools.map { contract ->
            MultiCallElement(
                contract.createFunction(
                    "earned",
                    listOf(address.toAddress()),
                    listOf(
                        TypeReference.create(Uint256::class.java)
                    )
                ),
                contract.address
            )
        }).mapIndexed { index, result ->
            val earned = result[0].value as BigInteger
            if (earned > BigInteger.ZERO) {
                val pool = pools[index]

                val reward = tokenService.getTokenInformation(pool.rewardsTokenAddress, getNetwork())

                val stakingToken = tokenService.getTokenInformation(pool.stakingTokenAddress, getNetwork())
                ClaimableElement(
                    address = pool.address,
                    type = "quickswap-reward-vault",
                    name = "${stakingToken.name} Rewards",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    claimableToken = ClaimableToken(
                        reward.name,
                        reward.symbol,
                        toDecimalValue(earned, reward.decimals)
                    )
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}