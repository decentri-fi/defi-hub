package io.codechef.defitrack.protocol.mstable

import com.fasterxml.jackson.databind.ObjectMapper
import io.codechef.defitrack.staking.UserStakingService
import io.codechef.defitrack.staking.domain.StakingElement
import io.codechef.defitrack.staking.domain.VaultRewardToken
import io.codechef.defitrack.token.TokenService
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mstable.MStableEthereumBoostedSavingsVaultContract
import io.defitrack.protocol.mstable.MStableEthereumService
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.util.*

@Service
class MStableEthereumStakingService(
    private val mStableEthereumService: MStableEthereumService,
    private val ethereumContractAccessor: EthereumContractAccessor,
    private val abiResource: ABIResource,
    tokenService: TokenService,
    objectMapper: ObjectMapper
) : UserStakingService(tokenService, objectMapper) {

    val boostedSavingsVaultABI by lazy {
        abiResource.getABI("mStable/BoostedSavingsVault.json")
    }

    override fun getStakings(address: String): List<StakingElement> {

        val vaultContracts = mStableEthereumService.getBoostedSavingsVaults().map {
            MStableEthereumBoostedSavingsVaultContract(
                ethereumContractAccessor,
                boostedSavingsVaultABI,
                it
            )
        }

        return vaultContracts.mapNotNull {
            val balance = it.rawBalanceOf(address)

            if (balance > BigInteger.ZERO) {
                val stakingToken = tokenService.getTokenInformation(it.stakingToken, getNetwork())
                val rewardsToken = tokenService.getTokenInformation(it.rewardsToken, getNetwork())
                StakingElement(
                    id = UUID.randomUUID().toString(),
                    network = this.getNetwork(),
                    protocol = this.getProtocol(),
                    name = it.name,
                    url = "https://etherscan.io/address/${it.address}",
                    stakedToken = vaultStakedToken(
                        stakingToken.address,
                        balance
                    ),
                    rewardTokens = listOf(
                        VaultRewardToken(
                            name = rewardsToken.name,
                            symbol = stakingToken.symbol,
                            url = "https://etherscan.io/address/${stakingToken.address}",
                        )
                    ),
                    contractAddress = it.address,
                    vaultType = "mstable-boosted-savings-vault"
                )
            } else {
                null
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.MSTABLE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}