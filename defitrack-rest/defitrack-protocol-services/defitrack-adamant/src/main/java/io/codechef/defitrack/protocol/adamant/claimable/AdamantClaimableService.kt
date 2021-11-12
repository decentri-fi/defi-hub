package io.codechef.defitrack.protocol.adamant.claimable

import io.codechef.defitrack.claimable.ClaimableElement
import io.codechef.defitrack.claimable.ClaimableService
import io.codechef.defitrack.claimable.ClaimableToken
import io.codechef.defitrack.price.PriceService
import io.codechef.defitrack.token.ERC20Resource
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.adamant.AdamantService
import io.defitrack.protocol.adamant.AdamantVaultContract
import io.defitrack.protocol.adamant.StrategyContract
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class AdamantClaimableService(
    private val abiResource: ABIResource,
    private val adamantService: AdamantService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val erC20Resource: ERC20Resource,
    private val priceService: PriceService
) : ClaimableService {

    val genericVaultABI = abiResource.getABI("adamant/GenericVault.json")

    override fun claimables(address: String): List<ClaimableElement> {

        val vaultContracts = adamantService.adamantGenericVaults().map {
            AdamantVaultContract(
                polygonContractAccessor,
                genericVaultABI,
                it.vaultAddress,
                it.lpAddress
            )
        }


        return polygonContractAccessor.readMultiCall(
            vaultContracts.map {
                MultiCallElement(
                    it.createFunction(
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
            val balance = result[0].value as BigInteger
            if (balance > BigInteger.ONE) {
                val vault = vaultContracts[index]

                val strategy = StrategyContract(
                    polygonContractAccessor,
                    abiResource.getABI("adamant/IStrategy.json"),
                    vault.strategy
                )

                val token = erC20Resource.getERC20(getNetwork(), strategy.feeDistToken)
                val pendingReward = vault.getPendingReward(address)
                val addyPrice = priceService.getPrice("ADDY")

                val amountInUsd = priceService.calculatePrice(
                    token.symbol,
                    pendingReward.toBigDecimal().divide(BigDecimal.TEN.pow(token.decimals), 18, RoundingMode.HALF_UP)
                        .toDouble()
                )

                ClaimableElement(
                    name = "Adamant Vault",
                    address = vault.address,
                    type = "adamant-vault",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    ClaimableToken(
                        name = "Addy",
                        symbol = "ADDY",
                        amount = amountInUsd.toBigDecimal().divide(addyPrice, 4, RoundingMode.HALF_UP)
                            .times(BigDecimal.valueOf(2L)).toDouble()
                    )
                )
            } else {
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