package io.defitrack.protocol.beefy.health

import io.defitrack.abi.ABIResource
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.beefy.BeefyService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.contract.StrategyMinichefLPContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.defitrack.protocol.reward.MiniChefV2Contract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class SushiPolygonHealthIndicator(
    private val beefyService: BeefyService,
    private val abiResource: ABIResource,
    private val polygonContractAccessor: PolygonContractAccessor
) {

    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    val miniChefABI by lazy {
        abiResource.getABI("sushi/MiniChefV2.json")
    }

    val StrategyMiniChefLP by lazy {
        abiResource.getABI("beefy/StrategyMiniChefLP.json")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(1)
    ).build<String, List<String>>()

    fun getEOL(): List<String> {
        return runBlocking {
            cache.get("quickswap-eol") {
                beefyService.beefyPolygonVaults.filter {
                    it.platform.lowercase() == "sushiswap" && it.chain.lowercase() == "polygon"
                }.filter {
                    it.status == "active"
                }.map(this@SushiPolygonHealthIndicator::beefyVaultToVaultContract)
                    .mapNotNull {
                        try {
                            val strat = it.strategy
                            val minichefStrat = asMinichefStrat(strat)
                            val rewardPoolAddress = minichefStrat.chef
                            val minichef = asMiniChef(rewardPoolAddress)

                            val accSushiPerShare = minichef.accSushiPerShare(minichefStrat.poolId.toInt())

                            val finished = accSushiPerShare == BigInteger.ZERO

                            if (finished) {
                                it.vaultId
                            } else {
                                null
                            }
                        } catch (ex: Exception) {
                            logger.error("Strat: ${it.strategy}")
                            logger.error(ex.message)
                            null
                        }
                    }
            }
        }
    }

    private fun asMinichefStrat(address: String): StrategyMinichefLPContract {
        return StrategyMinichefLPContract(
            polygonContractAccessor,
            StrategyMiniChefLP,
            address
        )
    }

    private fun asMiniChef(address: String): MiniChefV2Contract {
        return MiniChefV2Contract(
            polygonContractAccessor,
            miniChefABI,
            address
        )
    }

    private fun beefyVaultToVaultContract(beefyVault: BeefyVault) =
        BeefyVaultContract(
            polygonContractAccessor,
            vaultV6ABI,
            beefyVault.earnContractAddress,
            beefyVault.id
        )
}