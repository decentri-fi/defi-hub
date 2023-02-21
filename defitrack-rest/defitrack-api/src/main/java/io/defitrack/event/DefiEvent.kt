package io.defitrack.event

import io.defitrack.event.DefiEventType
import io.defitrack.protocol.Protocol

class DefiEvent(
    val type: DefiEventType,
    val protocol: Protocol? = null,
    val metadata: Map<String, Any>
)
