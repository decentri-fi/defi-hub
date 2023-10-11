package io.defitrack.claimable

import arrow.core.Either
import arrow.core.getOrElse
import io.defitrack.claimable.domain.ClaimableMarket
import io.defitrack.token.DecentrifiERC20Resource
import io.github.reactivecircus.cache4k.Cache
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

abstract class ClaimableMarketProvider {

    @Autowired
    protected lateinit var erC20Resource: DecentrifiERC20Resource

    abstract suspend fun fetchClaimables(): List<ClaimableMarket>

    private val logger = LoggerFactory.getLogger(this::class.java)

    val cache = Cache.Builder<String, ClaimableMarket>().build()

    fun getMarkets(): List<ClaimableMarket> {
        return Either.catch {
            val hashmap: Map<in String, ClaimableMarket> = HashMap(cache.asMap())
            hashmap.values.toMutableList()
        }.mapLeft {
            logger.error("Unable to get claimables", it)
        }.getOrElse { emptyList() }
    }

    suspend fun populateCaches() {
        Either.catch {
            fetchClaimables()
        }.mapLeft { ex ->
            logger.error("Unable to get claimables", ex)
        }.getOrElse { emptyList() }
            .forEach { market ->
                cache.put(market.id, market)
            }
        if (cache.asMap().isNotEmpty()) {
            logger.info("Claimables populated with ${cache.asMap().size} markets")
        }
    }
}