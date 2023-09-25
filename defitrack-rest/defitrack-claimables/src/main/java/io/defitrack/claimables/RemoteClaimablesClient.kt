package io.defitrack.claimables

import io.defitrack.claimable.ClaimableVO
import io.defitrack.protocol.ProtocolVO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!kubernetes")
class RemoteClaimablesClient(
    private val httpClient: HttpClient
) : ClaimablesClient {

    val baseUrl = "https://api.decentri.fi"

    override suspend fun getClaimables(address: String, protocol: ProtocolVO): List<ClaimableVO> = withContext(Dispatchers.IO) {
        val response = httpClient.get("$baseUrl/${protocol.slug}/$address/claimables")
        if (response.status.isSuccess()) {
            response.body()
        } else {
            emptyList()
        }
    }
}