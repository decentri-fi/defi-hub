package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.BeefyService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
@EnableScheduling
class BeefyPolygonStakingMarketService(
    private val contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val beefyPolygonService: BeefyService,
    private val erC20Resource: ERC20Resource,
    private val priceService: PriceResource
) : StakingMarketService() {

    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarketElement> {
        val vaultContracts = beefyPolygonService.beefyPolygonVaults
            .map(::beefyVaultToVaultContract)

        return vaultContracts.mapNotNull { beefyVault ->
            importVault(beefyVault)
        }
    }

    private fun importVault(beefyVault: BeefyVaultContract): StakingMarketElement? {
        return try {
            val want = erC20Resource.getTokenInformation(getNetwork(), beefyVault.want)

            val element = StakingMarketElement(
                id = beefyVault.vaultId,
                network = getNetwork(),
                protocol = getProtocol(),
                name = "${beefyVault.symbol} Beefy Vault",
                rate = getAPY(beefyVault),
                token = want.toFungibleToken(),
                reward = listOf(
                    want.toFungibleToken()
                ),
                contractAddress = beefyVault.address,
                marketSize = getMarketSize(want, beefyVault),
                vaultType = "beefyVaultV6"
            )
            logger.debug("adding ${element.id} to beefy vault list")
            element
        } catch (ex: Exception) {
            logger.error("Error trying to fetch vault metadata: ${ex.message}")
            null
        }
    }

    private fun getMarketSize(
        want: TokenInformation,
        beefyVault: BeefyVaultContract
    ) = BigDecimal.valueOf(
        priceService.calculatePrice(
            PriceRequest(
                want.address,
                getNetwork(),
                beefyVault.balance.toBigDecimal()
                    .divide(BigDecimal.TEN.pow(want.decimals), 18, RoundingMode.HALF_UP),
                want.type
            )
        )
    )


    private fun getAPY(beefyVault: BeefyVaultContract): BigDecimal {
        return try {
            (beefyAPYService.getAPYS().getOrDefault(beefyVault.vaultId, null)) ?: BigDecimal.ZERO
        } catch (ex: Exception) {
            BigDecimal.ZERO
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BEEFY
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    private fun beefyVaultToVaultContract(beefyVault: BeefyVault) =
        BeefyVaultContract(
            contractAccessorGateway.getGateway(getNetwork()),
            vaultV6ABI,
            beefyVault.earnContractAddress,
            beefyVault.id
        )
}