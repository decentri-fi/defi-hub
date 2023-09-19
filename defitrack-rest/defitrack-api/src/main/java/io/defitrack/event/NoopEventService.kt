package io.defitrack.event

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["rabbitmq.enabled"], havingValue = "false", matchIfMissing = true)
class NoopEventService : EventService{

    private val logger = LoggerFactory.getLogger(this::class.java)
    override fun publish(routeKey: String, event: Any) {
        logger.info("not sending event, no rabbit active")
    }
}