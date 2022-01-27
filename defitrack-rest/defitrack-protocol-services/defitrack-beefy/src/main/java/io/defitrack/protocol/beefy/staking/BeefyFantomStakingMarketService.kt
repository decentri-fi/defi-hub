package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.fantom.config.FantomContractAccessor
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.BeefyService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.RewardToken
import io.defitrack.staking.domain.StakedToken
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class BeefyFantomStakingMarketService(
    private val fantomContractAccessor: FantomContractAccessor,
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val beefyService: BeefyService,
    private val erC20Resource: ERC20Resource,
    private val priceService: PriceResource
) : StakingMarketService() {

    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    override fun fetchStakingMarkets(): List<StakingMarketElement> {
        return beefyService.beefyFantomVaults
            .map(this@BeefyFantomStakingMarketService::beefyVaultToVaultContract)
            .mapNotNull(this@BeefyFantomStakingMarketService::importVault)
    }

    private fun importVault(beefyVault: BeefyVaultContract): StakingMarketElement? {
        return try {
            val want = erC20Resource.getTokenInformation(getNetwork(), beefyVault.want)
            logger.debug("adding ${beefyVault.name} to beefy vault list")
            StakingMarketElement(
                id = beefyVault.vaultId,
                network = getNetwork(),
                protocol = getProtocol(),
                name = "${beefyVault.symbol} Beefy Vault",
                rate = getAPY(beefyVault),
                token = StakedToken(
                    name = want.name,
                    symbol = want.symbol,
                    address = want.address,
                    network = getNetwork(),
                    decimals = want.decimals,
                    type = want.type
                ),
                rewardToken = RewardToken(
                    name = want.name,
                    symbol = want.symbol,
                    decimals = want.decimals,
                ),
                contractAddress = beefyVault.address,
                marketSize = priceService.calculatePrice(
                    PriceRequest(
                        want.address,
                        getNetwork(),
                        beefyVault.balance.toBigDecimal()
                            .divide(BigDecimal.TEN.pow(want.decimals), 18, RoundingMode.HALF_UP),
                        want.type
                    )
                ),
                vaultType = "beefyVaultV6"
            )
        } catch (ex: Exception) {
            logger.error("Error trying to fetch vault metadata: ${ex.message}")
            null
        }
    }

    private fun getAPY(beefyVault: BeefyVaultContract): Double {
        return try {
            (beefyAPYService.getAPYS().getOrDefault(beefyVault.vaultId, null)?.toDouble()) ?: 0.0
        } catch (ex: Exception) {
            ex.printStackTrace()
            0.0
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BEEFY;
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }

    private fun beefyVaultToVaultContract(beefyVault: BeefyVault) =
        BeefyVaultContract(
            fantomContractAccessor,
            vaultV6ABI,
            beefyVault.earnContractAddress,
            beefyVault.id
        )
}