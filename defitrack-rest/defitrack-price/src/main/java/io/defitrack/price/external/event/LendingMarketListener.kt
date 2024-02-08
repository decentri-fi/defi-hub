package io.defitrack.price.external.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.defitrack.event.event.LendingMarketUpdatedEvent
import io.defitrack.event.event.PoolMarketUpdatedEvent
import io.defitrack.price.decentrifi.DecentrifiLendingPriceRepository
import io.defitrack.price.decentrifi.DecentrifiPoolingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
@ConditionalOnProperty(name = ["rabbitmq.enabled"], havingValue = "true", matchIfMissing = false)
@ConditionalOnBean(DecentrifiLendingPriceRepository::class)
class LendingMarketListener(
    private val decentrifiLendingPriceRepository: DecentrifiLendingPriceRepository
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    @Qualifier("priceLendingMarket")
    fun priceLendingMarketsQueue(): Queue {
        return Queue("price-lending-markets", false)
    }

    @Bean
    fun lendingUpdatedBinding(
        domainEventsExchange: TopicExchange,
        @Qualifier("priceLendingMarket") pricePoolingMarketsQueue: Queue
    ): Binding {
        return BindingBuilder.bind(pricePoolingMarketsQueue).to(domainEventsExchange).with("markets.lending.updated")
    }

    @RabbitListener(queues = ["price-lending-markets"])
    fun onLendingMarket(msg: Message) {
        val market = jacksonObjectMapper().readValue<LendingMarketUpdatedEvent>(msg.body)
        if (market.price > BigDecimal.ZERO && market.marketToken != null) {
            decentrifiLendingPriceRepository.putInCache(
                market.network,
                market.marketToken!!.address,
                market.price
            )
            logger.info(
                "Price for {} on {} updated to {}",
                market.marketToken!!.address,
                market.network.name,
                market.price
            )
        }
    }
}