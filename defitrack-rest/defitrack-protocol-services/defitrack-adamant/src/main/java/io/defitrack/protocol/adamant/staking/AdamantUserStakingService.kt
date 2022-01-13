package io.defitrack.protocol.adamant.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.adamant.AdamantService
import io.defitrack.protocol.adamant.AdamantVaultContract
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

@Service
class AdamantUserStakingService(
    erC20Resource: ERC20Resource,
    objectMapper: ObjectMapper,
    private val adamantService: AdamantService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiResource: ABIResource,
) : UserStakingService(
    erC20Resource,
    objectMapper
) {

    val genericVault by lazy {
        abiResource.getABI("adamant/GenericVault.json")
    }

    override fun getStakings(address: String): List<StakingElement> {

        val adamantVaultContracts = adamantService.adamantGenericVaults().map {
            AdamantVaultContract(
                polygonContractAccessor,
                genericVault,
                it.vaultAddress,
                it.lpAddress
            )
        }

        return polygonContractAccessor.readMultiCall(
            adamantVaultContracts.map {
                MultiCallElement(
                    it.createFunction(
                        "balanceOf",
                        listOf(address.toAddress()),
                        listOf(
                            TypeReference.create(Uint256::class.java)
                        )
                    ),
                    it.address
                )
            }
        ).mapIndexed { index, result ->
            val balance = result[0].value as BigInteger
            if (balance > BigInteger.ONE) {
                val vault = adamantVaultContracts[index]
                val wantAddress = vault.lpAddress
                val stakedToken = vaultStakedToken(
                    wantAddress,
                    vault.getTokensStaked(address)
                )

                stakingElement(
                    address,
                    "https://adamant.finance",
                    "Adamant ${stakedToken.name} Vault",
                    rewardTokens = emptyList(),
                    stakedToken = stakedToken,
                    "adamant-generic-vault",
                    vault.address
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