package io.defitrack.protocol.adamant.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.polygon.config.PolygonContractAccessorConfig
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.adamant.AdamantService
import io.defitrack.protocol.adamant.AdamantVaultContract
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AdamantUserStakingService(
    erC20Resource: ERC20Resource,
    private val adamantService: AdamantService,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
) : UserStakingService(
    erC20Resource
) {

    val genericVault by lazy {
        abiResource.getABI("adamant/GenericVault.json")
    }

    val gateway = contractAccessorGateway.getGateway(getNetwork())

    override fun getStakings(address: String): List<StakingElement> {

        val adamantVaultContracts = adamantService.adamantGenericVaults().map {
            AdamantVaultContract(
                gateway,
                genericVault,
                it.vaultAddress,
                it.lpAddress
            )
        }

        return erC20Resource.getBalancesFor(address, adamantVaultContracts.map { it.address }, gateway)
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val vault = adamantVaultContracts[index]
                    val stakedToken = erC20Resource.getTokenInformation(getNetwork(), vault.lpAddress)

                    stakingElement(
                        vaultName = "Adamant ${stakedToken.name} Vault",
                        rewardTokens = emptyList(),
                        stakedToken = stakedToken.toFungibleToken(),
                        vaultType = "adamant-generic-vault",
                        vaultAddress = vault.address,
                        amount = vault.getTokensStaked(address),
                        id = "adamant-generic-vault-$index"
                    )
                } else {
                    null
                }
            }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.ADAMANT
    }

    override fun getNetwork(): Network = Network.POLYGON
}