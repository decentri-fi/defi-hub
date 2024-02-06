package io.defitrack.price.decentrifi

import arrow.fx.coroutines.parMap
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import io.defitrack.market.domain.pooling.PoolingMarketInformation
import io.defitrack.price.PriceCalculator
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.external.ExternalPrice
import io.defitrack.price.port.out.Prices
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiPoolingPriceRepository(
    private val httpClient: HttpClient,
    private val prices: Prices
) {

    val logger = LoggerFactory.getLogger(this::class.java)
    val cache = Cache.Builder<String, ExternalPrice>().build()


    suspend fun PoolingMarketInformation.calculatePrice() : BigDecimal {
        return if(!hasBreakdown() || totalSupply == BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else {
            breakdown?.sumOf {
                prices.calculatePrice(
                    GetPriceCommand(
                        it.token.address,
                        it.token.network.toNetwork(),
                        it.reserveDecimal
                    )
                )
            }?.toBigDecimal() ?: BigDecimal.ZERO
        }
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 6)
    fun populatePoolPrices() = runBlocking {
        logger.info("fetching prices from decentrifi pools")
        val protocols = getProtocols()
        protocols.map { proto ->
            try {
                getPools(proto).filter {
                    it.hasBreakdown()
                }.parMap(concurrency = 12) { pool ->
                    toIndex(pool.network, pool.address) to ExternalPrice(
                        pool.address, pool.network.toNetwork(), pool.calculatePrice(), "decentrifi-pooling"
                    )
                }.forEach { pool ->
                    cache.put(
                        pool.first, pool.second
                    )
                }
            } catch (ex: Exception) {
                logger.error("Unable to import price for proto ${proto.slug}", ex)
            }
        }

        logger.info("Decentrifi Pooling Price Repository populated with ${cache.asMap().entries.size} prices")
    }

    fun putInCache(network: NetworkInformation, address: String, price: BigDecimal) =
        cache.put(
            toIndex(network, address), ExternalPrice(
                address, network.toNetwork(), price, "decentrifi-pooling"
            )
        )

    fun contains(token: FungibleTokenInformation): Boolean {
        return cache.get(toIndex(token.network, token.address)) != null
    }

    private fun toIndex(network: NetworkInformation, address: String) =
        "${network.name}-${address.lowercase()}"

    suspend fun getProtocols(): List<ProtocolInformation> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }

    suspend fun getPools(protocol: ProtocolInformation): List<PoolingMarketInformation> {
        val result =
            httpClient.get("https://api.decentri.fi/${protocol.slug}/pooling/all-markets")
        return if (result.status.isSuccess())
            result.body()
        else {
            logger.error("Unable to fetch pools for ${protocol.name} ${result.bodyAsText()}")
            emptyList()
        }
    }

    suspend fun getPrice(token: FungibleTokenInformation): BigDecimal {
        return cache.get(toIndex(token.network, token.address))?.price ?: BigDecimal.ZERO
    }
}
