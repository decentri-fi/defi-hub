package io.defitrack.protocol.beefy.staking

import io.defitrack.price.PriceRequest
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.RewardToken
import io.defitrack.staking.domain.StakedToken
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.TokenService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.abi.ABIResource
import io.defitrack.price.PriceResource
import io.defitrack.common.network.Network
import io.defitrack.fantom.config.FantomContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.BeefyService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class BeefyFantomStakingMarketService(
    private val fantomContractAccessor: FantomContractAccessor,
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val beefyService: BeefyService,
    private val tokenService: TokenService,
    private val priceService: PriceResource
) : StakingMarketService {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }


    @OptIn(ExperimentalTime::class)
    val cache =
        Cache.Builder().expireAfterWrite(Duration.Companion.hours(1)).build<String, List<StakingMarketElement>>()

    @PostConstruct
    fun startup() {
        Executors.newSingleThreadExecutor().submit {
            fetchVaults()
        }
    }

    fun fetchVaults(): List<StakingMarketElement> {
        return runBlocking {
            cache.get("vaults") {
                beefyService.beefyFantomVaults
                    .map(this@BeefyFantomStakingMarketService::beefyVaultToVaultContract)
                    .mapNotNull(this@BeefyFantomStakingMarketService::importVault)
            }
        }
    }

    private fun importVault(beefyVault: BeefyVaultContract): StakingMarketElement? {
        return try {
            val want = tokenService.getTokenInformation(beefyVault.want, getNetwork())
            logger.info("adding ${beefyVault.name} to beefy vault list")
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
            logger.error("Error trying to fetch vault metadata", ex)
            null
        }
    }

    override fun getStakingMarkets(): List<StakingMarketElement> = fetchVaults()

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