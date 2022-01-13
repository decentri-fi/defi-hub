package io.defitrack.protocol.quickswap.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.staking.domain.VaultRewardToken
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
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
class QuickswapUserStakingService(
    private val quickswapService: QuickswapService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiService: ABIResource,
    private val quickswapAPRService: QuickswapAPRService,
    objectMapper: ObjectMapper,
    tokenService: ERC20Resource,
) : UserStakingService(tokenService, objectMapper) {

    val stakingRewardsABI by lazy {
        abiService.getABI("quickswap/StakingRewards.json")
    }

    override fun getStakings(address: String): List<StakingElement> {
        return getRewardVaults(address)
    }

    private fun getRewardVaults(address: String): List<StakingElement> {
        val pools = quickswapService.getVaultAddresses().map {
            QuickswapRewardPoolContract(
                polygonContractAccessor,
                stakingRewardsABI,
                it
            )
        }

        return polygonContractAccessor.readMultiCall(pools.map { contract ->
            MultiCallElement(
                contract.createFunction(
                    "balanceOf",
                    listOf(address.toAddress()),
                    listOf(
                        TypeReference.create(Uint256::class.java)
                    )
                ),
                contract.address
            )
        }).mapIndexed { index, result ->
            val balance = result[0].value as BigInteger
            if (balance > BigInteger.ZERO) {

                val pool = pools[index]
                val stakedToken = erC20Resource.getTokenInformation(getNetwork(), pool.stakingTokenAddress)
                val rewardToken = erC20Resource.getTokenInformation(getNetwork(), pool.rewardsTokenAddress)

                stakingElement(
                    id = "quickswap-polygon-${pool.address}",
                    user = address.lowercase(),
                    vaultUrl = "https://quickswap.exchange",
                    vaultName = """${stakedToken.name} Reward""",
                    rewardTokens = listOf(
                        VaultRewardToken(
                            name = rewardToken.name,
                            decimals = rewardToken.decimals,
                            symbol = rewardToken.symbol,
                        )
                    ),
                    stakedToken = vaultStakedToken(stakedToken.address, balance),
                    vaultType = "quickswap-staking-rewards",
                    vaultAddress = pool.address,
                    rate = (quickswapAPRService.getRewardPoolAPR(pool.address) + quickswapAPRService.getLPAPR(
                        stakedToken.address
                    )).toDouble()
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