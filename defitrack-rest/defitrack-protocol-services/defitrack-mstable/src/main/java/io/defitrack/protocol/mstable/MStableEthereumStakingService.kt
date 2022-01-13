package io.defitrack.protocol.mstable

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.staking.domain.VaultRewardToken
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.util.*

@Service
class MStableEthereumStakingService(
    private val mStableEthereumService: MStableEthereumService,
    private val ethereumContractAccessor: EthereumContractAccessor,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource,
    objectMapper: ObjectMapper
) : UserStakingService(erC20Resource, objectMapper) {

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
                val stakingToken = erC20Resource.getTokenInformation(getNetwork(), it.stakingToken)
                val rewardsToken = erC20Resource.getTokenInformation(getNetwork(), it.rewardsToken)
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