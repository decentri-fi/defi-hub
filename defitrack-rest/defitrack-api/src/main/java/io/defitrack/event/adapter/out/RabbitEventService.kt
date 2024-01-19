package io.defitrack.event.adapter.out

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.event.EventService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["rabbitmq.enabled"], havingValue = "true", matchIfMissing = false)
class RabbitEventService(
    private val rabbitTemplate: RabbitTemplate,
    private val mapper: ObjectMapper
) : EventService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    override fun publish(routeKey: String, event: Any?) {
        if (event == null) {
            return
        }
        val serializedEvent = mapper.writeValueAsString(event)
        rabbitTemplate.convertAndSend(
            "domain-events",
            routeKey,
            serializedEvent
        )
        logger.debug("sent event $serializedEvent to $routeKey")
    }
}