package io.defitrack.price.external.adapter.decentrifi

import arrow.fx.coroutines.parMap
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.market.domain.pooling.PoolingMarketInformation
import io.defitrack.market.domain.pooling.PoolingMarketTokenShareInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.price.application.DefaultPriceCalculator
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import io.defitrack.protocol.ProtocolInformation
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnProperty("oracles.pooling_markets.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiPoolingPriceService(
    private val httpClient: HttpClient,
) : ExternalPriceService {

    override suspend fun getAllPrices(): Flow<ExternalPrice> = channelFlow {
        getPrices().forEach {
            send(it)
        }
    }

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val prices = mutableListOf<ExternalPrice>()

    @Autowired
    private lateinit var defaultPriceCalculator: DefaultPriceCalculator

    suspend fun AddMarketCommand.calculatePrice(): BigDecimal {
        return try {
            if (!hasBreakdown() || liquidity.isZero()) {
                BigDecimal.ZERO
            } else {
                val marketSize = breakdown?.sumOf {
                    defaultPriceCalculator.calculatePrice(
                        GetPriceCommand(
                            it.token.address,
                            it.token.network.toNetwork(),
                            it.reserveDecimal
                        )
                    )
                }?.toBigDecimal() ?: BigDecimal.ZERO
                marketSize.dividePrecisely(liquidity)
            }
        } catch (ex: Exception) {
            logger.debug("Unable to calculate price for {}, liquidity {}", name, liquidity)
            BigDecimal.ZERO
        }
    }

    fun getPrices() = runBlocking {
        logger.info("fetching prices from decentrifi pools")
        val protocols = getProtocols()
        protocols.map { proto ->
            try {
                val parMap = getPools(proto).parMap(concurrency = 12) { pool ->
                    AddMarketCommand(
                        breakdown = pool.breakdown,
                        liquidity = pool.totalSupply,
                        name = pool.name,
                        address = pool.address,
                        protocol = pool.protocol.slug,
                        network = pool.network
                    )
                }
                parMap.forEach {
                    addMarket(it)
                }
            } catch (ex: Exception) {
                logger.error("Unable to import price for proto ${proto.slug}", ex)
            }
        }

        logger.info("Decentrifi Pooling Price Repository populated with ${prices.size} prices")
        prices
    }

    suspend fun addMarket(addMarket: AddMarketCommand) {
        val price = addMarket.calculatePrice()
        logger.debug("Price for {} ({})  ({}) is {}", addMarket.name, addMarket.address, addMarket.protocol, price)
        putInCache(addMarket.network, addMarket.address, price, addMarket.name)
    }

    suspend fun createExternalPrice(addMarket: AddMarketCommand): ExternalPrice {
        val price = addMarket.calculatePrice()
        logger.debug("Price for {} ({})  ({}) is {}", addMarket.name, addMarket.address, addMarket.protocol, price)
        putInCache(addMarket.network, addMarket.address, price, addMarket.name)
        return ExternalPrice(
            addMarket.address, addMarket.network.toNetwork(), price, "decentrifi-pooling", addMarket.name, importOrder()
        )
    }

    private fun putInCache(
        network: NetworkInformation,
        address: String,
        price: BigDecimal,
        name: String
    ): ExternalPrice {
        val externalPrice = ExternalPrice(
            address, network.toNetwork(), price, "decentrifi-pooling", name, importOrder()
        )
        prices.add(
            externalPrice
        )
        return externalPrice
    }

    private suspend fun getProtocols(): List<ProtocolInformation> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }

    private suspend fun getPools(protocol: ProtocolInformation): List<PoolingMarketInformation> {
        val result =
            httpClient.get("https://api.decentri.fi/${protocol.slug}/pooling/all-markets")
        return if (result.status.isSuccess())
            result.body()
        else {
            logger.error("Unable to fetch pools for ${protocol.name} ${result.bodyAsText()}")
            emptyList()
        }
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