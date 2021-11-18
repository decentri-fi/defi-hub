package io.defitrack.protocol.beefy.staking

import io.codechef.defitrack.price.PriceRequest
import io.codechef.defitrack.staking.StakingMarketService
import io.codechef.defitrack.staking.domain.RewardToken
import io.codechef.defitrack.staking.domain.StakedToken
import io.codechef.defitrack.staking.domain.StakingMarketElement
import io.codechef.defitrack.token.TokenService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.abi.ABIResource
import io.defitrack.abi.PriceResource
import io.defitrack.common.network.Network
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.BeefyService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import okhttp3.internal.toImmutableList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Service
class BeefyPolygonStakingMarketService(
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val beefyPolygonService: BeefyService,
    private val tokenService: TokenService,
    private val priceService: PriceResource
) : StakingMarketService {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    val marketBuffer = mutableListOf<StakingMarketElement>()

    private val executor = Executors.newWorkStealingPool(8)

    @PostConstruct
    fun startup() {
        executor.submit {
            val vaultContracts = beefyPolygonService.beefyPolygonVaults
                .map(this::beefyVaultToVaultContract)

            vaultContracts.forEach { beefyVault ->
                executor.submit {
                    importVault(beefyVault)
                }
            }
        }
    }

    private fun importVault(beefyVault: BeefyVaultContract) {
        try {

            val want = tokenService.getTokenInformation(beefyVault.want, getNetwork())

            logger.info("adding ${beefyVault.name} to beefy vault list")

            val element = StakingMarketElement(
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
            marketBuffer.add(element)
        } catch (ex: Exception) {
            logger.error("Error trying to fetch vault metadata", ex)
        }
    }

    override fun getStakingMarkets(): List<StakingMarketElement> = marketBuffer.toImmutableList()

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
        return Network.POLYGON
    }

    private fun beefyVaultToVaultContract(beefyVault: BeefyVault) =
        BeefyVaultContract(
            polygonContractAccessor,
            vaultV6ABI,
            beefyVault.earnContractAddress,
            beefyVault.id
        )
}