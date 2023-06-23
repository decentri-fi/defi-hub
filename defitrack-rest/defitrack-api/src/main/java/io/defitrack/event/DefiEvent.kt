package io.defitrack.event

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.Protocol

class DefiEvent(
    val transactionId: String,
    val network: NetworkVO,
    val type: DefiEventType,
    val protocol: Protocol? = null,
    val metadata: Map<String, Any>
)
