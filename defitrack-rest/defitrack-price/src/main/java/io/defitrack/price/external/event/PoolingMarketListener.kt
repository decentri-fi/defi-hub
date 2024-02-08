package io.defitrack.price.external.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.event.event.PoolMarketUpdatedEvent
import io.defitrack.market.domain.pooling.PoolingMarketTokenShareInformation
import io.defitrack.price.PriceCalculator
import io.defitrack.price.decentrifi.DecentrifiLendingPriceRepository
import io.defitrack.price.decentrifi.DecentrifiPoolingPriceRepository
import io.defitrack.price.domain.GetPriceCommand
import io.ktor.util.Identity.decode
import kotlinx.coroutines.runBlocking
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
@ConditionalOnBean(DecentrifiPoolingPriceRepository::class)
class PoolingMarketListener(
    private val decentrifiPoolingPriceRepository: DecentrifiPoolingPriceRepository,
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
        try {
            val market = jacksonObjectMapper().readValue<PoolMarketUpdatedEvent>(msg.body)
            logger.info("we should now calculate the price for ${market.id} and update it in the cache")

            when {
                market.totalSupply.isZero() -> {
                    logger.info("Skipping market ${market.id} (${market.protocol}) because total supply is zero")
                }

                market.breakdown.isNullOrEmpty() -> {
                    logger.info("Skipping market ${market.id} (${market.protocol}) because breakdown is empty")
                }

                market.erc20Compatible != true -> {
                    logger.info("Skipping market ${market.id} (${market.protocol}) because it is not erc20 compatible")
                }

                else -> {
                    decentrifiPoolingPriceRepository.addMarket(
                        DecentrifiPoolingPriceRepository.AddMarketCommand(
                            breakdown = market.breakdown?.map {
                                PoolingMarketTokenShareInformation(
                                    token = it.token,
                                    reserve = it.reserve,
                                    reserveDecimal = it.reserve.asEth(it.token.decimals)
                                )
                            },
                            address = market.address,
                            liquidity = market.totalSupply,
                            name = market.name ?: "unknown",
                            protocol = market.protocol,
                            network = market.network
                        )
                    )
                }
            }
        } catch (ex: Exception) {
            logger.error("Error while processing pooling market event: {}", ex.message)
        }
    }
}