package io.defitrack.claimables

import io.defitrack.claimable.vo.ClaimableMarketVO
import io.defitrack.claimable.vo.UserClaimableVO
import io.defitrack.protocol.ProtocolVO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!kubernetes")
class RemoteClaimablesClient(
    private val httpClient: HttpClient
) : ClaimablesClient {

    val baseUrl = "https://api.decentri.fi"

    private val logger = LoggerFactory.getLogger(this::class.java)


    override suspend fun getClaimables(address: String, protocol: ProtocolVO): List<UserClaimableVO> =
        withContext(Dispatchers.IO) {
            try {
                val response = httpClient.get("$baseUrl/${protocol.slug}/$address/claimables")
                if (response.status.isSuccess()) {
                    response.body()
                } else {
                    emptyList()
                }
            } catch (ex: Exception) {
                logger.error("Error getting claimables for $address on ${protocol.company.slug}/${protocol.slug}", ex)
                emptyList()
            }
        }

    override suspend fun getClaimableMarkets(protocol: ProtocolVO): List<ClaimableMarketVO> =
        withContext(Dispatchers.IO) {
            try {
                val response = httpClient.get("$baseUrl/${protocol.slug}/claimables")
                if (response.status.isSuccess()) {
                    response.body()
                } else {
                    emptyList()
                }
            } catch (ex: Exception) {
                logger.error("Error getting claimables markets on ${protocol.company.slug}/${protocol.slug}", ex)
                emptyList()
            }
        }
}