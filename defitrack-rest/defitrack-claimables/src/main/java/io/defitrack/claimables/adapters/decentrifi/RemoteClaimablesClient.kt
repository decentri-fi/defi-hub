package io.defitrack.claimables.adapters.decentrifi

import arrow.fx.coroutines.parMap
import io.defitrack.claimable.vo.ClaimableMarketVO
import io.defitrack.claimable.vo.UserClaimableVO
import io.defitrack.claimables.ports.outputs.ClaimablesClient
import io.defitrack.protocol.Protocol
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.hours
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

@Component
@Profile("!grouped-fetchers")
class RemoteClaimablesClient(
    private val httpClient: HttpClient
) : ClaimablesClient {

    val baseUrl = "https://api.decentri.fi"
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getClaimables(address: String, protocols: List<Protocol>): List<UserClaimableVO> =
        withContext(Dispatchers.IO) {
            protocols.parMap(concurrency = 12) { protocol ->
                val timedValue: TimedValue<List<UserClaimableVO>> = measureTimedValue {
                    try {
                        val response = httpClient.get("$baseUrl/${protocol.slug}/$address/claimables")
                        if (response.status.isSuccess()) {
                            response.body()
                        } else {
                            emptyList()
                        }
                    } catch (ex: Exception) {
                        logger.error(
                            "Error getting claimables for $address on ${protocol.company.slug}/${protocol.slug}",
                            ex
                        )
                        emptyList()
                    }
                }

                logger.info("took ${timedValue.duration} to get claimables for $address on ${protocol.slug}")
                timedValue.value
            }.flatten()
        }

    val cache = Cache.Builder<String, List<ClaimableMarketVO>>().expireAfterWrite(1.hours).build()
    override suspend fun getClaimableMarkets(protocol: Protocol): List<ClaimableMarketVO> {
        return cache.get(protocol.slug) {
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
    }
}