package io.defitrack.price.external.adapter.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.defitrack.adapter.output.domain.market.PoolingMarketTokenShareInformationDTO
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.event.PoolMarketUpdatedEvent
import io.defitrack.price.application.PriceAggregator
import io.defitrack.price.external.adapter.decentrifi.DecentrifiPoolingPriceService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["rabbitmq.enabled"], havingValue = "true", matchIfMissing = false)
@ConditionalOnBean(DecentrifiPoolingPriceService::class)
class PoolingMarketListener(
    private val priceAggregator: PriceAggregator,
    private val decentrifiPoolingPriceService: DecentrifiPoolingPriceService,
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

    @RabbitListener(queues = ["price-pooling-market-updated"])
    fun onPoolingMarketAdded(msg: Message) = runBlocking {
        try {
            val market = jacksonObjectMapper().readValue<PoolMarketUpdatedEvent>(msg.body)
            when {
                market.totalSupply.isZero() -> {
                    logger.debug("Skipping market ${market.id} (${market.protocol}) because total supply is zero")
                }

                market.breakdown.isNullOrEmpty() -> {
                    logger.debug("Skipping market ${market.id} (${market.protocol}) because breakdown is empty")
                }

                market.erc20Compatible != true -> {
                    logger.debug("Skipping market ${market.id} (${market.protocol}) because it is not erc20 compatible")
                }

                else -> {
                    val externalPrice = decentrifiPoolingPriceService.createExternalPrice(
                        DecentrifiPoolingPriceService.AddMarketCommand(
                            breakdown = market.breakdown,
                            address = market.address,
                            liquidity = market.totalSupply,
                            name = market.name ?: "unknown",
                            protocol = market.protocol,
                            network = market.network
                        )
                    )
                    priceAggregator.addPrice(externalPrice)
                }
            }
        } catch (ex: Exception) {
            logger.error("Error while processing pooling market event: {}", ex.message)
        }
    }
}