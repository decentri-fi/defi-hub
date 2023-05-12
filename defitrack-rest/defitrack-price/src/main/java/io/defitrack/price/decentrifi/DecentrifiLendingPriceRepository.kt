package io.defitrack.price.decentrifi

import io.defitrack.market.lending.vo.LendingMarketVO
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
class DecentrifiLendingPriceRepository(
    private val httpClient: HttpClient
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    val cache = Cache.Builder().build<String, BigDecimal>()

    @Scheduled(fixedDelay = 1000 * 60 * 10)
    fun populatePoolPrices() = runBlocking {
        val protocols = getProtocols()
        protocols.map { protocol ->
            try {
                val pools = getLendingMarkets(protocol.slug)
                pools
                    .filter {
                        it.erc20Compatible && it.marketToken != null
                    }
                    .forEach { market ->
                        val price = market.price
                        if (price == null) {
                            logger.error("Price for market ${market.name} in ${market.protocol.name} is null")
                        } else {
                            cache.put(market.marketToken!!.address.lowercase(), price)
                        }
                    }
            } catch (ex: Exception) {
                logger.error("Unable to import price for protocol ${protocol.slug}", ex)
            }
        }

        logger.info("Decentrifi Lending Price Repository populated with ${cache.asMap().entries.size} prices")
    }

    fun contains(address: String): Boolean {
        return cache.get(address.lowercase()) != null
    }

    suspend fun getProtocols(): List<ProtocolVO> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }

    suspend fun getLendingMarkets(protocol: String): List<LendingMarketVO> {
        val result = httpClient.get("https://api.decentri.fi/$protocol/lending/all-markets")
        return if (result.status.isSuccess())
            result.body()
        else {
            logger.error("Unable to fetch lending markets for $protocol")
            emptyList()
        }
    }

    suspend fun getPrice(address: String): BigDecimal {
        return cache.get(address.lowercase()) ?: BigDecimal.ZERO
    }
}