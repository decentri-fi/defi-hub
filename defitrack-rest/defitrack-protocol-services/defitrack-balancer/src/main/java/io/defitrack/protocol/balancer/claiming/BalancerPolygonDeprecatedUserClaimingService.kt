package io.defitrack.protocol.balancer.claiming

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableService
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.polygon.BalancerPolygonPoolGraphProvider
import io.defitrack.protocol.balancer.domain.LiquidityMiningReward
import io.defitrack.token.ERC20Resource
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import kotlin.time.Duration.Companion.days

class BalancerPolygonDeprecatedUserClaimingService(
    private val balancerPolygonPoolGraphProvider: BalancerPolygonPoolGraphProvider,
    private val erC20Resource: ERC20Resource
) : ClaimableService {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    val cache = Cache.Builder()
        .expireAfterWrite(1.days)
        .build<String, List<LiquidityMiningReward>>()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 18)
    fun init() {
        try {
            logger.info("cache empty, populating all rewards")
            cache.invalidateAll()
            val rewards = getAll()
            logger.info("done populating ${rewards.size} rewards")
        } catch (ex: Exception) {
            logger.error("something went wrong trying to populate the cache", ex)
        }
    }

    override suspend fun claimables(address: String): List<Claimable> {
        return getAll().filter {
            it.user.lowercase() == address.lowercase()
        }.map {
            val token = erC20Resource.getTokenInformation(getNetwork(), it.token)
            Claimable(
                "balancer-polygon-${it.week}-${it.token}",
                "${token.symbol} reward",
                it.token,
                "balancer-lp-reward",
                getProtocol(),
                getNetwork(),
                token.toFungibleToken(),
                it.amount.toBigInteger()
            )
        }
    }

    private fun getAll() = runBlocking {
        cache.get("all") {
            balancerPolygonPoolGraphProvider.getDeprecatedRewards()
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}