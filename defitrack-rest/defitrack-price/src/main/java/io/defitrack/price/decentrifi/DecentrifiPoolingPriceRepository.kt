package io.defitrack.price.decentrifi

import arrow.fx.coroutines.parMap
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import io.defitrack.market.domain.pooling.PoolingMarketInformation
import io.defitrack.market.domain.pooling.PoolingMarketTokenShareInformation
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnProperty("oracles.pooling_markets.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiPoolingPriceRepository(
    private val httpClient: HttpClient,
    private val prices: Prices
) {

    val logger = LoggerFactory.getLogger(this::class.java)
    val cache = Cache.Builder<String, ExternalPrice>().build()


    suspend fun AddMarketCommand.calculatePrice(): BigDecimal {
        return if (!hasBreakdown() || liquidity == BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else {
            val marketSize = breakdown?.sumOf {
                prices.calculatePrice(
                    GetPriceCommand(
                        it.token.address,
                        it.token.network.toNetwork(),
                        it.reserveDecimal
                    )
                )
            }?.toBigDecimal() ?: BigDecimal.ZERO
            marketSize.dividePrecisely(liquidity)
        }
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 1)
    fun populatePoolPrices() = runBlocking {
        logger.info("fetching prices from decentrifi pools")
        val protocols = getProtocols()
        protocols.map { proto ->
            try {
                getPools(proto).parMap(concurrency = 8) { pool ->
                    AddMarketCommand(
                        breakdown = pool.breakdown,
                        liquidity = pool.totalSupply,
                        name = pool.name,
                        address = pool.address,
                        protocol = proto.slug,
                        network = pool.network
                    )
                }.forEach {
                    addMarket(it)
                }
            } catch (ex: Exception) {
                logger.error("Unable to import price for proto ${proto.slug}", ex)
            }
        }

        logger.info("Decentrifi Pooling Price Repository populated with ${cache.asMap().entries.size} prices")
    }

    suspend fun addMarket(addMarket: AddMarketCommand) {
        val price = addMarket.calculatePrice()
        logger.debug("Price for {} ({})  ({}) is {}", addMarket.name, addMarket.address, addMarket.protocol, price)
        putInCache(addMarket.network, addMarket.address, price, addMarket.name)
    }

    private fun putInCache(network: NetworkInformation, address: String, price: BigDecimal, name: String) =
        cache.put(
            toIndex(network, address), ExternalPrice(
                address, network.toNetwork(), price, "decentrifi-pooling", name
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

    class AddMarketCommand(
        val breakdown: List<PoolingMarketTokenShareInformation>?,
        val liquidity: BigDecimal,
        val name: String,
        val address: String,
        val protocol: String,
        val network: NetworkInformation
    ) {

        fun hasBreakdown(): Boolean {
            return !breakdown.isNullOrEmpty()
        }
    }
}
