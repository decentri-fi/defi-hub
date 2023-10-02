package io.defitrack.claimable

import ClaimableMarketProvider
import io.github.reactivecircus.cache4k.Cache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class Claimables(private val claimableMarketProvider: List<ClaimableMarketProvider>) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val cache = Cache.Builder<String, ClaimableMarket>().build()

    fun getMarkets(): List<ClaimableMarket> {
        return try {
            val hashmap: Map<in String, ClaimableMarket> = HashMap(cache.asMap())
            hashmap.values.toMutableList()
        } catch (ex: Exception) {
            logger.error("Unable to get markets from map", ex)
            emptyList()
        }
    }

    suspend fun populate() {
        claimableMarketProvider.forEach {
            it.getClaimables().forEach { market ->
                cache.put(market.id, market)
            }
        }
        logger.info("Claimables populated with ${cache.asMap().size} markets")
    }
}