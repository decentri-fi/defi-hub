package io.defitrack.adapter.output

import io.defitrack.adapter.output.domain.meta.ProtocolInformationDTO
import io.defitrack.port.output.ProtocolClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.springframework.stereotype.Component

@Component
internal class DecentrifiProtocolRestClient(
    private val httpClient: HttpClient
) : ProtocolClient {
    override suspend fun getProtocols(): List<ProtocolInformationDTO> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }
}