package io.defitrack.claim

import arrow.core.Either.Companion.catch
import arrow.core.getOrElse
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractClaimableMarketProvider {

    @Autowired
    protected lateinit var erC20Resource: ERC20Resource

    @Autowired
    protected lateinit var blockchainGatewayProvider: BlockchainGatewayProvider

    abstract suspend fun fetchClaimables(): List<ClaimableMarket>

    private val logger = LoggerFactory.getLogger(this::class.java)

    val cache = Cache.Builder<String, ClaimableMarket>().build()

    fun getMarkets(): List<ClaimableMarket> {
        return catch {
            val hashmap: Map<in String, ClaimableMarket> = HashMap(cache.asMap())
            hashmap.values.toMutableList()
        }.mapLeft {
            logger.error("Unable to get claimables", it)
        }.getOrElse { emptyList() }
    }

    suspend fun populateCaches() {
        catch {
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