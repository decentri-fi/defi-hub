package io.defitrack.price.external.adapter.decentrifi

import io.defitrack.adapter.output.domain.market.LendingMarketInformationDTO
import io.defitrack.common.network.Network
import io.defitrack.port.output.MarketClient
import io.defitrack.port.output.ProtocolClient
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnProperty("oracles.lending_markets.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiLendingPriceService(
    private val decentrifiProtocols: ProtocolClient,
    private val markets: MarketClient,
) : ExternalPriceService {


    override suspend fun getAllPrices(): Flow<ExternalPrice> = channelFlow {
        getPrices().forEach {
            send(it)
        }
    }

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val prices = mutableListOf<ExternalPrice>()

    fun getPrices() = runBlocking {
        logger.info("fetching prices from decentrifi lending pools")
        val protocols = decentrifiProtocols.getProtocols()
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
                            putInCache(market.network.toNetwork(), market.marketToken!!.address, price, market.name)
                        }
                    }
            } catch (ex: Exception) {
                logger.error("Unable to import price for protocol ${protocol.slug}", ex)
            }
        }

        logger.info("Decentrifi Lending Price Repository populated with ${prices.size} prices")
        prices
    }

    fun putInCache(network: Network, address: String, price: BigDecimal, name: String) {
        prices.add(
            ExternalPrice(
                address.lowercase(), network, price, "decentrifi-lending", name, importOrder()
            )
        )
    }

    suspend fun getLendingMarkets(protocol: String): List<LendingMarketInformationDTO> {
        return markets.getLendingMarkets(protocol)
    }
}