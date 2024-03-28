package io.defitrack.port.output

import io.defitrack.adapter.output.domain.meta.ProtocolInformationDTO

interface ProtocolClient {

    suspend fun getProtocols(): List<ProtocolInformationDTO>
}
