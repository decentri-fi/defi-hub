package io.defitrack.price.decentrifi

import io.defitrack.domain.FungibleToken
import io.defitrack.domain.NetworkInformation
import io.defitrack.domain.ProtocolInformation
import io.defitrack.market.lending.vo.LendingMarketVO
import io.defitrack.price.external.ExternalPrice
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
class DecentrifiLendingPriceRepository(
    private val httpClient: HttpClient
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    val cache = Cache.Builder<String, ExternalPrice>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 6)
    fun populatePoolPrices() = runBlocking {
        logger.info("fetching prices from decentrifi lending pools")
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
                            putInCache(market.network, market.marketToken!!.address, price)
                        }
                    }
            } catch (ex: Exception) {
                logger.error("Unable to import price for protocol ${protocol.slug}", ex)
            }
        }

        logger.info("Decentrifi Lending Price Repository populated with ${cache.asMap().entries.size} prices")
    }

    fun putInCache(network: NetworkInformation, address: String, price: BigDecimal) {
        cache.put(
            toIndex(network, address.lowercase()), ExternalPrice(
                address.lowercase(), network.toNetwork(), price, "decentrifi-lending"
            )
        )
    }

    fun contains(token: FungibleToken): Boolean {
        return cache.get(toIndex(token.network, token.address)) != null
    }

    private fun toIndex(network: NetworkInformation, address: String) = "${network.name}-${address.lowercase()}"

    suspend fun getProtocols(): List<ProtocolInformation> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }

    suspend fun getLendingMarkets(protocol: String): List<LendingMarketVO> {
        val result = httpClient.get("https://api.decentri.fi/$protocol/lending/all-markets")
        return if (result.status.isSuccess())
            result.body()
        else {
            logger.error(
                "Unable to fetch lending markets for $protocol (${result.bodyAsText()}"
            )
            emptyList()
        }
    }

    suspend fun getPrice(token: FungibleToken): BigDecimal {
        return cache.get(toIndex(token.network, token.address))?.price ?: BigDecimal.ZERO
    }
}
