package io.defitrack.protocol.quickswap.claimable

import io.defitrack.abi.ABIResource
import io.defitrack.claimable.ClaimableElement
import io.defitrack.claimable.ClaimableService
import io.defitrack.claimable.ClaimableToken
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapRewardPoolContract
import io.defitrack.quickswap.QuickswapService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

@Service
class QuickswapClaimableService(
    private val quickswapService: QuickswapService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiService: ABIResource,
    private val erC20Resource: ERC20Resource
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

                val reward = erC20Resource.getTokenInformation(getNetwork(), pool.rewardsTokenAddress)

                val stakingToken = erC20Resource.getTokenInformation(getNetwork(), pool.stakingTokenAddress)
                ClaimableElement(
                    id = "quickswap-reward-$pool.address",
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