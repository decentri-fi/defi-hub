package io.defitrack.price.external.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.defitrack.market.event.PoolMarketUpdatedEvent
import io.defitrack.price.decentrifi.DecentrifiPoolingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
@ConditionalOnProperty(name = ["rabbitmq.enabled"], havingValue = "true", matchIfMissing = false)
class PoolingMarketListener(
    private val decentrifiPoolingPriceRepository: DecentrifiPoolingPriceRepository
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun pricePoolingMarketsQueue(): Queue {
        return Queue("price-pooling-markets", false)
    }

    @Bean
    fun poolingUpdatedBinding(domainEventsExchange: TopicExchange, newUserQueue: Queue): Binding {
        return BindingBuilder.bind(newUserQueue).to(domainEventsExchange).with("markets.pooling.updated")
    }

    @RabbitListener(queues = ["price-pooling-markets"])
    fun onPoolingMarketAdded(msg: Message) {
        val market = jacksonObjectMapper().readValue<PoolMarketUpdatedEvent>(msg.body)
        if (market.price > BigDecimal.ZERO) {
            decentrifiPoolingPriceRepository.putInCache(
                market.network,
                market.address,
                market.price
            )
            logger.debug("Price for {} in {} updated to {}", market.address, market.network.name, market.price)
        }
    }
}