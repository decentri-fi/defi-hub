package io.codechef.protocol.beefy.health

import io.codechef.defitrack.abi.ABIResource
import io.codechef.matic.config.PolygonContractAccessor
import io.codechef.protocol.beefy.BeefyService
import io.codechef.protocol.beefy.contract.BeefyVaultContract
import io.codechef.protocol.beefy.contract.StrategyPolygonQuickLPContract
import io.codechef.protocol.beefy.domain.BeefyVault
import io.codechef.quickswap.QuickswapRewardPoolContract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class QuickswapHealthIndicator(
    private val beefyService: BeefyService,
    private val abiResource: ABIResource,
    private val polygonContractAccessor: PolygonContractAccessor
) {

    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    val stakingRewardsABI by lazy {
        abiResource.getABI("quickswap/StakingRewards.json")
    }

    val strategyPolygonQuickLPABI by lazy {
        abiResource.getABI("beefy/StrategyPolygonQuickLP.json")
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
                    it.platform.lowercase() == "quickswap"
                }.filter {
                    it.status == "active"
                }.map(this@QuickswapHealthIndicator::beefyVaultToVaultContract)
                    .mapNotNull {
                        try {
                            val strat = it.strategy
                            val rewardPoolAddress = asRewardPoolStrat(strat).rewardPool
                            val stakingRewards = asStakingRewards(rewardPoolAddress)
                            val finished =
                                Date(stakingRewards.periodFinish.toLong()).toInstant().atZone(ZoneId.systemDefault())
                                    .isAfter(
                                        LocalDateTime.now().atZone(ZoneId.systemDefault())
                                    )
                            if (finished) {
                                it.vaultId
                            } else {
                                null
                            }
                        } catch (ex: Exception) {
                            logger.error(ex.message)
                            null
                        }
                    }
            }
        }
    }

    private fun asRewardPoolStrat(address: String): StrategyPolygonQuickLPContract {
        return StrategyPolygonQuickLPContract(
            polygonContractAccessor,
            strategyPolygonQuickLPABI,
            address
        )
    }

    private fun asStakingRewards(address: String): QuickswapRewardPoolContract {
        return QuickswapRewardPoolContract(
            polygonContractAccessor,
            stakingRewardsABI,
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