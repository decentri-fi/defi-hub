package io.codechef.defitrack.protocol.quickswap.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.codechef.common.network.Network
import io.codechef.defitrack.abi.ABIResource
import io.codechef.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.codechef.defitrack.staking.UserStakingService
import io.codechef.defitrack.staking.domain.StakingElement
import io.codechef.defitrack.staking.domain.VaultRewardToken
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
class QuickswapUserStakingService(
    private val quickswapService: QuickswapService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiService: ABIResource,
    private val quickswapAPRService: QuickswapAPRService,
    objectMapper: ObjectMapper,
    tokenService: TokenService,
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
                val stakedToken = tokenService.getTokenInformation(pool.stakingTokenAddress, getNetwork())
                val rewardToken = tokenService.getTokenInformation(pool.rewardsTokenAddress, getNetwork())

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
                            url = "https://polygonscan.com/address/${rewardToken.address}",
                        )
                    ),
                    stakedToken = vaultStakedToken(stakedToken.address, balance),
                    vaultType = "quickswap-staking-rewards",
                    vaultAddress = pool.address,
                    rate = (quickswapAPRService.getRewardPoolAPR(pool.address) + quickswapAPRService.getLPAPR(stakedToken.address)).toDouble()
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