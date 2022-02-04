package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.bsc.BscContractAccessor
import io.defitrack.common.network.Network
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.BeefyService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.defitrack.protocol.staking.Token
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.RewardToken
import io.defitrack.staking.domain.StakedToken
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.math.RoundingMode

@Service
class BeefyBscStakingMarketService(
    private val bscContractAccessor: BscContractAccessor,
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
        return beefyPolygonService.beefyBscVaults
            .map(this::beefyVaultToVaultContract)
            .mapNotNull { beefyVault ->
                toStakingMarketElement(beefyVault)
            }
    }

    private fun toStakingMarketElement(beefyVault: BeefyVaultContract): StakingMarketElement? {
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
                reward = listOf(
                    RewardToken(
                        name = want.name,
                        symbol = want.symbol,
                        decimals = want.decimals,
                    )
                ),
                contractAddress = beefyVault.address,
                marketSize = getMarketSize(want, beefyVault),
                vaultType = "beefyVaultV6"
            )
        } catch (ex: Exception) {
            logger.error("Error trying to fetch vault metadata ${beefyVault.vaultId}: ${ex.message}")
            null
        }
    }

    private fun getMarketSize(
        want: Token,
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
            (beefyAPYService.getAPYS().getOrDefault(beefyVault.vaultId, null)) ?: ZERO
        } catch (ex: Exception) {
            ZERO
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BEEFY;
    }

    override fun getNetwork(): Network {
        return Network.BSC
    }

    private fun beefyVaultToVaultContract(beefyVault: BeefyVault) =
        BeefyVaultContract(
            bscContractAccessor,
            vaultV6ABI,
            beefyVault.earnContractAddress,
            beefyVault.id
        )
}