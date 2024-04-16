package io.defitrack

import io.defitrack.event.EventService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["rabbitmq.enabled"], havingValue = "false", matchIfMissing = true)
class NoopEventService : EventService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    override fun publish(routeKey: String, event: Any?) {
        if (event == null) {
            return
        }
        logger.debug("not sending event, no rabbit active")
    }
}