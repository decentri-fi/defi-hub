package io.defitrack.price.decentrifi

import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.protocol.ProtocolVO
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiPoolingPriceRepository(
    private val httpClient: HttpClient
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    val cache = Cache.Builder().build<String, BigDecimal>()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun populatePoolPrices() = runBlocking {
        val protocols = getProtocols()
        protocols.map { protocol ->
            try {
                val pools = getPools(protocol.slug)
                pools.map { pool ->
                    val price = pool.price
                    if (price == null) {
                        logger.debug("Price for pool ${pool.address} in ${pool.protocol.name} is null")
                    } else {
                        cache.put(pool.address.lowercase(), price)
                    }
                }
            } catch (ex: Exception) {
                logger.error("Unable to import price for protocol ${protocol.slug}", ex)
            }
        }
    }

    fun contains(address: String): Boolean {
        return cache.get(address.lowercase()) != null
    }

    suspend fun getProtocols(): List<ProtocolVO> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }

    suspend fun getPools(protocol: String): List<PoolingMarketVO> {
        val result = httpClient.get("https://api.decentri.fi/$protocol/pooling/all-markets")
        if (result.status.isSuccess())
            return result.body()
        else {
            logger.error("Unable to fetch pools for $protocol")
            return emptyList()
        }
    }

    suspend fun getPrice(address: String): BigDecimal {
        return cache.get(address.lowercase()) ?: BigDecimal.ZERO
    }
}
