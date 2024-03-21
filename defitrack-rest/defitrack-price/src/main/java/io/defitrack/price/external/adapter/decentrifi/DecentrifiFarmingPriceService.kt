package io.defitrack.price.external.adapter.decentrifi

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.marketinfo.port.out.Markets
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import io.defitrack.protocol.port.`in`.ProtocolResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnProperty("oracles.farming_markets.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiFarmingPriceService(
    private val marketResource: Markets,
    private val protocolResource: ProtocolResource
) : ExternalPriceService {

    override suspend fun getAllPrices(): Flow<ExternalPrice> = channelFlow {
        getPrices().forEach {
            send(it)
        }
    }

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val prices = mutableListOf<ExternalPrice>()

    fun getPrices() = runBlocking {
        protocolResource.getProtocols().map { proto ->
            try {
                marketResource.getFarmingMarkets(proto.slug)
                    .filter {
                        it.token != null && (it.marketSize ?: BigDecimal.ZERO) > BigDecimal.ONE
                    }.parMapNotNull(concurrency = 12) { farm ->

                        val supply = farm.token!!.totalSupply.asEth(farm.token!!.decimals)

                        if (supply == BigDecimal.ZERO) {
                            return@parMapNotNull null
                        }

                        val pricePerToken = (farm.marketSize ?: BigDecimal.ZERO).dividePrecisely(
                            supply
                        )

                        ExternalPrice(
                            farm.token!!.address,
                            farm.network.toNetwork(),
                            pricePerToken,
                            "decentrifi-farming",
                            farm.name,
                            importOrder()
                        )
                    }.forEach { price ->
                        prices.add(
                            price
                        )
                    }
            } catch (ex: Exception) {
                logger.error("Unable to import price for proto ${proto.slug}", ex)
            }
        }

        logger.info("Decentrifi Farming Price Repository populated with ${prices.size} prices")
        prices
    }
}