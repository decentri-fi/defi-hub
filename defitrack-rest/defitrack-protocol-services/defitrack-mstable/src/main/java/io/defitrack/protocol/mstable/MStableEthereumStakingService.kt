package io.defitrack.protocol.mstable

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class MStableEthereumStakingService(
    private val mStableEthereumService: MStableEthereumService,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource,
) : UserStakingService(erC20Resource) {

    val boostedSavingsVaultABI by lazy {
        abiResource.getABI("mStable/BoostedSavingsVault.json")
    }

    override fun getStakings(address: String): List<StakingElement> {
        val gateway = contractAccessorGateway.getGateway(getNetwork())

        val vaultContracts = mStableEthereumService.getBoostedSavingsVaults().map {
            MStableEthereumBoostedSavingsVaultContract(
                gateway,
                boostedSavingsVaultABI,
                it
            )
        }

        return erC20Resource.getBalancesFor(address, vaultContracts.map { it.address }, getNetwork())
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
                        stakedToken = stakingToken.toFungibleToken(),
                        rewardTokens = listOf(
                            rewardsToken.toFungibleToken()
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