package io.defitrack.price.decentrifi

import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.protocol.ProtocolVO
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal
import javax.annotation.PostConstruct

@Component
class DecentrifiPoolingPriceRepository(
    private val httpClient: HttpClient
) {

    val cache = Cache.Builder().build<String, BigDecimal>()

    @PostConstruct
    fun populatePoolPrices() = runBlocking {
        val protocols = getProtocols()
        protocols.map { protocol ->
            val pools = getPools(protocol.slug)
            pools.map { pool ->
                val price = pool.price
                cache.put(pool.address.lowercase(), price)
            }
        }
    }

    fun appliesTo(address: String): Boolean {
        return cache.get(address.lowercase()) != null
    }

    suspend fun getProtocols(): List<ProtocolVO> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }

    suspend fun getPools(protocol: String): List<PoolingMarketVO> {
        return httpClient.get("https://api.decentri.fi/$protocol/pooling/all-markets").body()
    }

    suspend fun getPrice(address: String): BigDecimal {
        return cache.get(address.lowercase()) ?: BigDecimal.ZERO
    }
}
