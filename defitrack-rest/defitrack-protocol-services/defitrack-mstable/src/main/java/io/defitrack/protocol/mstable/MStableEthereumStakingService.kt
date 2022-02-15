package io.defitrack.protocol.mstable

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.RewardToken
import io.defitrack.staking.domain.StakingElement
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
) : UserStakingService(erC20Resource) {

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
                    stakedToken = stakedToken(
                        stakingToken.address,
                    ),
                    rewardTokens = listOf(
                        RewardToken(
                            name = rewardsToken.name,
                            symbol = stakingToken.symbol,
                            decimals = rewardsToken.decimals
                        )
                    ),
                    contractAddress = it.address,
                    vaultType = "mstable-boosted-savings-vault",
                    amount = balance
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