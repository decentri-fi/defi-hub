package io.defitrack.claimables

import io.defitrack.claimable.UserClaimableVO
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
@Profile("kubernetes")
class LocalClaimablesClient(
    private val httpClient: HttpClient
) : ClaimablesClient {

    override suspend fun getClaimables(address: String, protocol: ProtocolVO): List<UserClaimableVO> = withContext(Dispatchers.IO) {
        val response = httpClient.get("http://defitrack-${protocol.company.slug}.default.svc.cluster.local:8080/${protocol.slug}/$address/claimables")
        if (response.status.isSuccess()) {
            response.body()
        } else {
            emptyList()
        }
    }
}