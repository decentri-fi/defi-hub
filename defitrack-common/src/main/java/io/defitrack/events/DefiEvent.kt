package io.defitrack.events

import io.defitrack.protocol.Protocol

class DefiEvent(
    val type: DefiEventType,
    val protocol: Protocol? = null,
    val metadata: Map<String, Any>
)
