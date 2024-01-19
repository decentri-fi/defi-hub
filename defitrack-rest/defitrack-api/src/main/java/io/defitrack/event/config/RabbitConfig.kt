package io.defitrack.event.config

import org.springframework.amqp.core.TopicExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class RabbitConfig {
    @Bean
    fun domainEventsExchange(): TopicExchange {
        return TopicExchange("domain-events", true, false)
    }
}