package io.defitrack.price.external.adapter.decentrifi

import io.defitrack.market.domain.lending.LendingMarketInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import io.defitrack.protocol.ProtocolInformation
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnProperty("oracles.lending_markets.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiLendingPriceService(
    private val httpClient: HttpClient
) : ExternalPriceService {

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return getPrices()
    }

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val prices = mutableListOf<ExternalPrice>()

    fun getPrices() = runBlocking {
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
                            putInCache(market.network, market.marketToken!!.address, price, market.name)
                        }
                    }
            } catch (ex: Exception) {
                logger.error("Unable to import price for protocol ${protocol.slug}", ex)
            }
        }

        logger.info("Decentrifi Lending Price Repository populated with ${prices.size} prices")
        prices
    }

    fun putInCache(network: NetworkInformation, address: String, price: BigDecimal, name: String) {
        prices.add(
            ExternalPrice(
                address.lowercase(), network.toNetwork(), price, "decentrifi-lending", name, order()
            )
        )
    }

    suspend fun getProtocols(): List<ProtocolInformation> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }

    suspend fun getLendingMarkets(protocol: String): List<LendingMarketInformation> {
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

}