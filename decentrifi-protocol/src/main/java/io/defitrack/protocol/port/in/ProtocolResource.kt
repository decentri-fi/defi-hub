package io.defitrack.protocol.port.`in`

import io.defitrack.protocol.ProtocolInformation

interface ProtocolResource {
    suspend fun getProtocols(): List<ProtocolInformation>
}