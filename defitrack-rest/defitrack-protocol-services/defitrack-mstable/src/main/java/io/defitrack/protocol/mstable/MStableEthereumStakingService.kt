package io.defitrack.protocol.mstable

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

        return erC20Resource.getBalancesFor(address, vaultContracts.map { it.address }, ethereumContractAccessor)
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val contract = vaultContracts[index]
                    val stakingToken = erC20Resource.getTokenInformation(getNetwork(), contract.stakingToken)
                    val rewardsToken = erC20Resource.getTokenInformation(getNetwork(), contract.rewardsToken)
                    StakingElement(
                        id = "mstable-ethereum-${contract.address}",
                        network = this.getNetwork(),
                        protocol = this.getProtocol(),
                        name = contract.name,
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
                        contractAddress = contract.address,
                        vaultType = "mstable-boosted-savings-vault",
                        amount = balance
                    )
                } else {
                    null
                }
            }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.MSTABLE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}