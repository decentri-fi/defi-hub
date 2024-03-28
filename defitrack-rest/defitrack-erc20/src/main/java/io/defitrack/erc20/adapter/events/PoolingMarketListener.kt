package io.defitrack.erc20.adapter.events

import arrow.core.Option
import arrow.core.getOrElse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.defitrack.erc20.application.ERC20TokenService
import io.defitrack.erc20.application.TokenCache
import io.defitrack.erc20.domain.TokenInformation
import io.defitrack.event.PoolMarketUpdatedEvent
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = ["rabbitmq.enabled"], havingValue = "true", matchIfMissing = false)
class PoolingMarketListener(
    private val erC20TokenService: ERC20TokenService,
    private val tokenCache: TokenCache
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    @Qualifier("erc20PoolingMarket")
    fun pricePoolingMarketsQueue(): Queue {
        return Queue("erc20-pooling-market-updated", false)
    }

    @Bean
    fun poolingUpdatedBinding(
        domainEventsExchange: TopicExchange,
        @Qualifier("erc20PoolingMarket") erc20PoolingMarket: Queue
    ): Binding {
        return BindingBuilder.bind(erc20PoolingMarket).to(domainEventsExchange).with("markets.pooling.updated")
    }

    @RabbitListener(queues = ["erc20-pooling-market-updated"])
    fun onPoolingMarketAdded(msg: Message) = runBlocking {
        try {
            val market = jacksonObjectMapper().readValue<PoolMarketUpdatedEvent>(msg.body)
            handleMarketUpdated(market)
        } catch (ex: Exception) {
            logger.error("Error while processing pooling market event: {}", ex.message)
        }
    }

    private suspend fun handleMarketUpdated(market: PoolMarketUpdatedEvent) {
        try {

            if (market.erc20Compatible == true && market.breakdown != null) {
                val underlying = market.breakdown!!
                    .map { share ->
                        erC20TokenService.fetchTokenInfo(share.token.network.toNetwork(), share.token.address)
                            .map { info ->
                                TokenInformation(
                                    network = share.token.network.toNetwork(),
                                    logo = share.token.logo,
                                    name = share.token.name,
                                    symbol = share.token.symbol,
                                    address = share.token.address,
                                    decimals = share.token.decimals,
                                    type = share.token.type,
                                    totalSupply = info.totalSupply
                                )
                            }
                    }.mapNotNull {
                        it.getOrNull()
                    }


                val asToken = erC20TokenService.fetchTokenInfo(market.network.toNetwork(), market.address)
                val tokenInfo = TokenInformation(
                    network = market.network.toNetwork(),
                    name = market.name ?: underlying.joinToString("/") { it.name },
                    symbol = underlying.joinToString("/") { it.symbol },
                    address = market.address,
                    protocol = Protocol.fromString(market.protocol),
                    decimals = asToken.map { it.decimals }.getOrElse { 18 },
                    type = TokenType.CUSTOM_LP,
                    underlyingTokens = underlying,
                )

                tokenCache.put(market.address, market.network.toNetwork(), Option.fromNullable(tokenInfo))
                logger.info("pool market updated: {} - {}", market.name, market.address)
            }
        } catch (ex: Exception) {
            logger.error("Unable to process pooling market event: {}", ex.message)
        }
    }
}