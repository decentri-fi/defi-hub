package io.defitrack.protocol

import io.defitrack.protocol.port.`in`.ProtocolResource
import org.springframework.stereotype.Component

@Component
class DecentrifiProtocolResourceAdapter(
    private val protocols: Protocols
) : ProtocolResource {
    override suspend fun getProtocols(): List<ProtocolInformation> {
        return protocols.getProtocols()
    }
}