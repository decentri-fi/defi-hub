package io.codechef.protocol.beefy.staking

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.codechef.common.network.Network
import io.codechef.defitrack.abi.ABIResource
import io.codechef.defitrack.price.PriceRequest
import io.codechef.defitrack.price.PriceService
import io.codechef.defitrack.staking.StakingMarketService
import io.codechef.defitrack.staking.domain.RewardToken
import io.codechef.defitrack.staking.domain.StakedToken
import io.codechef.defitrack.staking.domain.StakingMarketElement
import io.codechef.defitrack.token.TokenService
import io.codechef.ethereum.config.ArbitrumContractAccessor
import io.codechef.protocol.Protocol
import io.codechef.protocol.beefy.BeefyService
import io.codechef.protocol.beefy.apy.BeefyAPYService
import io.codechef.protocol.beefy.contract.BeefyVaultContract
import io.codechef.protocol.beefy.domain.BeefyVault
import kotlinx.coroutines.runBlocking
import okhttp3.internal.toImmutableList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Service
class BeefyArbitrumStakingMarketService(
    private val arbitrumContractAccessor: ArbitrumContractAccessor,
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val beefyService: BeefyService,
    private val tokenService: TokenService,
    private val priceService: PriceService
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
            val vaultContracts = beefyService.beefyArbitrumVaults
                .map(this::beefyVaultToVaultContract)

            vaultContracts.forEach { beefyVault ->
                executor.submit {
                    importVault(beefyVault)
                }
            }
        }
    }

    private fun importVault(beefyVault: BeefyVaultContract)  {
        try {

            val want =  tokenService.getTokenInformation(beefyVault.want, getNetwork())

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
        return Network.ARBITRUM
    }

    private fun beefyVaultToVaultContract(beefyVault: BeefyVault) =
        BeefyVaultContract(
            arbitrumContractAccessor,
            vaultV6ABI,
            beefyVault.earnContractAddress,
            beefyVault.id
        )
}