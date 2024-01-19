package io.defitrack.adapter.output

import io.defitrack.protocol.ProtocolInformation
import io.defitrack.protocol.Protocols
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.springframework.stereotype.Component

@Component
internal class DecentrifiProtocolRestClient(
    private val httpClient: HttpClient
) : Protocols {
    override suspend fun getProtocols(): List<ProtocolInformation> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }
}