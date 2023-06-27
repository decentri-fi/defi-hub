package io.defitrack.price.decentrifi

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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

    val cache = Cache.Builder<String, BigDecimal>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 10)
    fun populatePoolPrices() = runBlocking {
        val protocols = getProtocols()
        protocols.map { proto ->
            try {
                val pools = getPools(proto)
                pools.forEach { pool ->
                    val price = pool.price
                    if (price == null) {
                        logger.error("Price for pool ${pool.address} in ${pool.protocol.name} is null")
                    } else {
                        cache.put(toIndex(pool.network, pool.address), price)
                    }
                }
            } catch (ex: Exception) {
                logger.error("Unable to import price for proto ${proto.slug}", ex)
            }
        }

        logger.info("Decentrifi Pooling Price Repository populated with ${cache.asMap().entries.size} prices")
    }

    fun contains(token: TokenInformationVO): Boolean {
        return cache.get(toIndex(token.network, token.address)) != null
    }

    private fun toIndex(network: NetworkVO, address: String) =
        "${network.name}-${address.lowercase()}"

    suspend fun getProtocols(): List<ProtocolVO> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }

    suspend fun getPools(protocol: ProtocolVO): List<PoolingMarketVO> {
        val result =
            httpClient.get("https://api.decentri.fi/${protocol.slug}/pooling/all-markets")
        return if (result.status.isSuccess())
            result.body()
        else {
            logger.error("Unable to fetch pools for ${protocol.name} ${result.bodyAsText()}")
            emptyList()
        }
    }

    suspend fun getPrice(token: TokenInformationVO): BigDecimal {
        return cache.get(toIndex(token.network, token.address)) ?: BigDecimal.ZERO
    }
}
