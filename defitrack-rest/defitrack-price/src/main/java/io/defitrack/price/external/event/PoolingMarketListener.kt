package io.defitrack.price.external.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.event.event.PoolMarketUpdatedEvent
import io.defitrack.price.PriceCalculator
import io.defitrack.price.decentrifi.DecentrifiPoolingPriceRepository
import io.defitrack.price.domain.GetPriceCommand
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
@ConditionalOnProperty(name = ["rabbitmq.enabled"], havingValue = "true", matchIfMissing = false)
class PoolingMarketListener(
    private val decentrifiPoolingPriceRepository: DecentrifiPoolingPriceRepository,
    private val priceCalculator: PriceCalculator
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    @Qualifier("pricePoolingMarket")
    fun pricePoolingMarketsQueue(): Queue {
        return Queue("price-pooling-market-updated", false)
    }

    @Bean
    fun poolingUpdatedBinding(
        domainEventsExchange: TopicExchange,
        @Qualifier("pricePoolingMarket") pricePoolingMarketsQueue: Queue
    ): Binding {
        return BindingBuilder.bind(pricePoolingMarketsQueue).to(domainEventsExchange).with("markets.pooling.updated")
    }

    @RabbitListener(queues = ["price-pooling-markets"])
    fun onPoolingMarketAdded(msg: Message) = runBlocking {
        val market = jacksonObjectMapper().readValue<PoolMarketUpdatedEvent>(msg.body)
        logger.info("we should now calculate the price for ${market.id} and update it in the cache")

        when {
            market.totalSupply.isZero() -> {
                logger.info("Skipping market ${market.id} (${market.protocol}) because total supply is zero")
            }
            market.breakdown.isNullOrEmpty() -> {
                logger.info("Skipping market ${market.id} (${market.protocol}) because breakdown is empty")
            }
            else -> {
                val marketSize = market.breakdown?.sumOf {
                    priceCalculator.calculatePrice(
                        GetPriceCommand(
                            it.token.address,
                            it.token.network.toNetwork(),
                            it.reserve.asEth(it.token.decimals)
                        )
                    )
                }?.toBigDecimal() ?: BigDecimal.ZERO

                val price = marketSize.dividePrecisely(market.totalSupply)
                logger.info("new price for ${market.id} is $price")

                decentrifiPoolingPriceRepository.putInCache(
                    market.network,
                    market.address,
                    price
                )
            }
        }
    }
}