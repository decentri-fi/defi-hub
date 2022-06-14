package io.defitrack.protocol.adamant.claimable

import io.defitrack.abi.ABIResource
import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableService
import io.defitrack.claimable.PrepareClaimCommand
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.adamant.AdamantVaultContract
import io.defitrack.protocol.adamant.staking.AdamantVaultMarketService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

@Service
class AdamantClaimableService(
    private val abiResource: ABIResource,
    private val adamantVaultMarketService: AdamantVaultMarketService,
    blockchainGatewayProvider: BlockchainGatewayProvider,
    private val erC20Resource: ERC20Resource
) : ClaimableService {

    val genericVaultABI = abiResource.getABI("adamant/GenericVault.json")

    val gateway = blockchainGatewayProvider.getGateway(getNetwork())

    val addy by lazy {
        erC20Resource.getTokenInformation(getNetwork(), "0xc3fdbadc7c795ef1d6ba111e06ff8f16a20ea539")
    }

    override suspend fun claimables(address: String): List<Claimable> {

        val markets = adamantVaultMarketService.getStakingMarkets().map {
            AdamantVaultContract(
                gateway,
                genericVaultABI,
                it.contractAddress,
            )
        }.take(100)

        return gateway.readMultiCall(
            markets.map {
                MultiCallElement(
                    it.createFunctionWithAbi(
                        "getPendingReward",
                        listOf(address.toAddress()),
                        listOf(
                            TypeReference.create(Uint256::class.java)
                        )
                    ),
                    it.address
                )
            }
        ).mapIndexed { index, result ->
            try {
                val balance = result[0].value as BigInteger
                if (balance > BigInteger.ONE) {
                    val vault = markets[index]

                    val pendingReward = vault.getPendingReward(address)

                    Claimable(
                        name = "Adamant Vault",
                        address = vault.address,
                        type = "adamant-vault",
                        protocol = getProtocol(),
                        network = getNetwork(),
                        id = "adamant-vault-claim-${vault.address}",
                        claimableToken = addy.toFungibleToken(),
                        amount = pendingReward,
                        claimTransaction = AdamantVaultClaimPreparer(
                            vault
                        ).prepare(
                            PrepareClaimCommand(
                                user = address
                            )
                        )
                    )
                } else {
                    null
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.ADAMANT
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}